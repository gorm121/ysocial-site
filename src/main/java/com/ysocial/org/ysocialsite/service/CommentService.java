package com.ysocial.org.ysocialsite.service;


import com.ysocial.org.ysocialsite.dto.ProfileShortDto;
import com.ysocial.org.ysocialsite.dto.response.CommentResponse;
import com.ysocial.org.ysocialsite.entites.Comment;
import com.ysocial.org.ysocialsite.entites.Post;
import com.ysocial.org.ysocialsite.entites.Profile;
import com.ysocial.org.ysocialsite.exceptions.EntityNotFoundException;
import com.ysocial.org.ysocialsite.repository.PostRepository;
import com.ysocial.org.ysocialsite.repository.ProfileRepository;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.time.LocalDateTime;

@Service
@Slf4j
public class CommentService {
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;


    public CommentService(PostRepository postRepository, ProfileRepository profileRepository, UserService userService) {
        this.postRepository = postRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public CommentResponse createComment(CustomUserDetails userDetails,
                                         Long postId, String commentText) {
        Long currentUserId = userDetails.getId();

        Profile profile = profileRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Профиль не найден"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Пост не найден"));

        Comment comment = new Comment();
        comment.setAuthorId(currentUserId);
        comment.setText(commentText);
        comment.setCreatedAt(LocalDateTime.now());

        post.getComments().add(comment);

        postRepository.save(post);
        String name = profile.getFirstName() + " " + profile.getLastName();
        String avatarUrl = "/images/default-avatar.png";

        return new CommentResponse(comment, new ProfileShortDto(currentUserId, name, avatarUrl));
    }     
}