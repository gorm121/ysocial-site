package com.ysocial.org.ysocialsite.service;

import com.ysocial.org.ysocialsite.dto.ProfileShortDto;
import com.ysocial.org.ysocialsite.dto.request.CreatePostRequest;
import com.ysocial.org.ysocialsite.dto.response.CommentResponse;
import com.ysocial.org.ysocialsite.dto.response.PostResponse;
import com.ysocial.org.ysocialsite.entities.*;
import com.ysocial.org.ysocialsite.enums.FriendshipStatus;
import com.ysocial.org.ysocialsite.enums.ReactionType;
import com.ysocial.org.ysocialsite.enums.UserRole;
import com.ysocial.org.ysocialsite.exceptions.EntityNotFoundException;
import com.ysocial.org.ysocialsite.repository.*;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final FriendshipRepository friendshipRepository;
    private final ProfileRepository profileRepository;
    private final PostReactionRepository postReactionRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository,
                       FriendshipRepository friendshipRepository,
                       ProfileRepository profileRepository,
                       PostReactionRepository postReactionRepository,
                       UserRepository userRepository
    ) {
        this.postRepository = postRepository;
        this.friendshipRepository = friendshipRepository;
        this.profileRepository = profileRepository;
        this.postReactionRepository = postReactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getFeed(CustomUserDetails userDetails, int page, int size) {
        User currentUser = userDetails.getUser();
        Long currentUserId = currentUser.getId();

        // Собираем айдишники наших друзей
        List<Long> friendIds = friendshipRepository.findFriendIdsWithStatus(currentUserId, FriendshipStatus.ACCEPTED);
        friendIds.add(currentUserId); // Добавляем еще и себя

        // Pageable тут нужен лишь для PageImpl, который нужен для фронтенда (hasNext())
        Pageable pageable = PageRequest.of(page, size);

        long totalElements = postRepository.countNewsFeed(friendIds);
        if (totalElements == 0) {
            // если нет ничо отдаем пустой Page
            return Page.empty(pageable);
        }

        // ручной расчет offset, потому что в Spring Data JDBC автоматическая пагинация (Pageable)
        // часто конфликтует со сложными SQL-запросами, содержащими JOIN и IN
        long offset = (long) page * size;
        List<Long> postIds = postRepository.findNewsFeedIds(friendIds, size, offset);

        //если нет постов, отдаем пустой Page
        if (postIds.isEmpty()) return Page.empty(pageable);

        // Загружаем посты с сохранением порядка сортировки из ленты
        // простой WHERE id IN (:ids) может перемешать посты,
        // поэтому делаем сортировку по created_at DESC, id DESC
        // так же там еще подгрузятся реакции и комменты к постам, через @MappedCollection(idColumn = "post_id")
        // и @MappedCollection(idColumn = "post_id", keyColumn = "comment_order") соответственно
        List<Post> posts = postRepository.findAllByIdsSorted(postIds);

        //делаем set так как у нас может быть 5 постов от одного чела
        Set<Long> userIdsToFetch = posts.stream().map(Post::getAuthorId).collect(Collectors.toSet());

        // тут добавляем еще челов из комментов
        posts.forEach(post ->
                post.getComments().forEach(comment -> userIdsToFetch.add(comment.getAuthorId()))
        );

        // собираем наши профили одним запросом
        Map<Long, Profile> profilesMap = profileRepository.findAllByUsersId(new ArrayList<>(userIdsToFetch))
                .stream().collect(Collectors.toMap(Profile::getUserId, p -> p));

        // Маппим посты в DTO, передавая Map с профилями для быстрого доступа
        List<PostResponse> dtos = posts.stream()
                .map(post -> mapToDto(post, profilesMap, currentUserId))
                .toList();


        return new PageImpl<>(dtos, pageable, totalElements);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getUserFeed(CustomUserDetails userDetails, Long userId, int page, int size) {
        User viewer = userDetails.getUser();

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Профиль не найден"));

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );
        
        // Если профиль закрытый, и мы не владелец профиля, и просто пользователь, то проверяем статус дружбы
        if (profile.isPrivate() && !viewer.getId().equals(userId) && viewer.getRole() == UserRole.USER) {
            Optional<Friendship> friendshipOpt = friendshipRepository.findFriendshipBetween(viewer.getId(), userId);
            if (friendshipOpt.isEmpty() || friendshipOpt.get().getStatus() != FriendshipStatus.ACCEPTED) {
                // Если пользователь не друг, отдаем пустой Page
                return Page.empty(pageable);
            }
        }
        
        Page<Post> postsPage = postRepository.findByAuthorId(userId, pageable);

        if (postsPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Post> posts = postsPage.getContent();

        Set<Long> userIdsToFetch = new HashSet<>();
        userIdsToFetch.add(userId);
        // Собираем всех пользователей которые присутствуют в комментах, чтобы потом одним запросом загрузить их профили
        posts.forEach(post ->
                post.getComments().forEach(comment -> userIdsToFetch.add(comment.getAuthorId()))
        );

        // Затем собираем их профили
        Map<Long, Profile> profilesMap = profileRepository.findAllByUsersId(new ArrayList<>(userIdsToFetch))
                .stream().collect(Collectors.toMap(Profile::getUserId, p -> p));
        
        List<PostResponse> dtos = posts.stream()
                .map(post -> mapToDto(post, profilesMap, viewer.getId()))
                .toList();

        return new PageImpl<>(dtos, pageable, postsPage.getTotalElements());
    }


    @Transactional
    public PostResponse processReaction(CustomUserDetails userDetails, Long postId, ReactionType type) {
        User currentUser = userDetails.getUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Пост не найден"));

        // ищем существующую рекцию по id юзера
        PostReaction existingReaction = post.getReactions().stream()
                .filter(r -> r.getUserId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);

        // еслии нет вообще реакции то создаем ее
        if (existingReaction == null) {
            PostReaction newReaction = new PostReaction();
            newReaction.setUserId(currentUser.getId());
            newReaction.setType(type);
            post.getReactions().add(newReaction); 
        } else {
            // если была реакция которую на фронте нажал юзер то убираем ее
            if (existingReaction.getType() == type) {
                post.getReactions().remove(existingReaction);
                postReactionRepository.delete(existingReaction);
            } else {
                // иначе меняем на другую
                existingReaction.setType(type); 
            }
        }
        
        Post savedPost = postRepository.save(post);

        Set<Long> userIdsToFetch = new HashSet<>();
        userIdsToFetch.add(savedPost.getAuthorId());
        savedPost.getComments().forEach(c -> userIdsToFetch.add(c.getAuthorId()));
        
        Map<Long, Profile> profilesMap = profileRepository.findAllByUsersId(new ArrayList<>(userIdsToFetch))
                .stream().collect(Collectors.toMap(Profile::getUserId, p -> p));
        
        return mapToDto(savedPost, profilesMap, currentUser.getId());
    }

    @Transactional
    public boolean createPost(CustomUserDetails userDetails,
                              CreatePostRequest request,
                              MultipartFile image) {
        User currentUser = userDetails.getUser();

        Post post = new Post();
        post.setAuthorId(currentUser.getId());
        post.setContent(request.getContent());
        post.setCreatedAt(LocalDateTime.now());

        if (image != null && !image.isEmpty()) {
            post.setImageUrl(null);
        }

        postRepository.save(post);
        return true;
    }

    @Transactional
    public void deletePostByUser(CustomUserDetails userDetails, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Пост не найден"));

        // если автор то удаляем
        if (post.getAuthorId().equals(userDetails.getId())) {
            postRepository.delete(post);
            return;
        }

        // если не автор то по ролям будем проверять
        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        boolean isStaff = List.of(UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.MODERATOR)
                .contains(currentUser.getRole());

        if (!isStaff) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "У вас нет прав на удаление чужого поста");
        }

        postRepository.delete(post);
    }

    private PostResponse mapToDto(Post post, Map<Long, Profile> profilesMap, Long currentUserId) {
        // Считаем реакции
        long likes = post.getReactions().stream().filter(r -> r.getType() == ReactionType.LIKE).count();
        long dislikes = post.getReactions().stream().filter(r -> r.getType() == ReactionType.DISLIKE).count();

        // Ищем реакцию текущего пользователя
        ReactionType myReaction = post.getReactions().stream()
                .filter(r -> r.getUserId().equals(currentUserId))
                .map(PostReaction::getType)
                .findFirst().orElse(null);
        // получаем профиль за O(1)
        // Без Map пришлось бы делать profileRepository.findByUserId() для каждого поста
        Profile authorProfile = profilesMap.get(post.getAuthorId());
        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .likesCount(likes)
                .dislikesCount(dislikes)
                .reactionType(myReaction)
                .author(toProfileInPostDto(authorProfile))
                .comments(post.getComments().stream()
                        .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                        .map(c -> {
                            // O(1)  профиль комментатора из той же Map
                            Profile commentAuthor = profilesMap.get(c.getAuthorId());
                            return new CommentResponse(c, toProfileInPostDto(commentAuthor));
                        }).toList())
                .createdAt(post.getCreatedAt() != null ? post.getCreatedAt().format(DateTimeFormatter.ofPattern("d MMMM, HH:mm")) : "")
                .imageUrl("/images/default-avatar.png")
                .build();
    }


    public ProfileShortDto toProfileInPostDto(Profile profile) {
        String name = profile.getFirstName() + " " + profile.getLastName();
        String avatarUrl = "/images/default-avatar.png";
        return new ProfileShortDto(profile.getUserId(), name, avatarUrl);
    }
}