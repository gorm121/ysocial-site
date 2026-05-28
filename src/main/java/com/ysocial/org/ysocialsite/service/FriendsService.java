package com.ysocial.org.ysocialsite.service;


import com.ysocial.org.ysocialsite.entities.Friendship;
import com.ysocial.org.ysocialsite.enums.AccountStatus;
import com.ysocial.org.ysocialsite.enums.FriendshipStatus;
import com.ysocial.org.ysocialsite.exceptions.BadRequestException;
import com.ysocial.org.ysocialsite.exceptions.EntityNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ysocial.org.ysocialsite.dto.FriendDto;
import com.ysocial.org.ysocialsite.entities.User;
import com.ysocial.org.ysocialsite.repository.FriendshipRepository;
import com.ysocial.org.ysocialsite.repository.UserRepository;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;

import java.util.List;

@Service
public class FriendsService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final StorageService storageService;
    private final EmailService emailService;

    public FriendsService(FriendshipRepository friendshipRepository,
                          UserRepository userRepository,
                          ProfileRepository profileRepository,
                          StorageService storageService,
                          EmailService emailService)
    {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.storageService = storageService;
        this.emailService = emailService;
    }

    @Transactional
    public List<FriendDto> getRequests(CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        
        return friendshipRepository.findRequestsWithProfiles(userId)
            .stream().map( f -> 
                new FriendDto(
                    f.id(),
                    f.name(),
                    storageService.getAvatarUrl(f.avatarUrl()),
                    f.addedAt())
                ).toList();    
    }

    @Transactional
    public List<FriendDto> getFriends(CustomUserDetails userDetails) {
        Long currentUserId = userDetails.getId();

        return friendshipRepository.findFriendsWithProfiles(currentUserId).stream()
                .map(f -> {
                    return new FriendDto(
                        f.id(),
                        f.name(),
                        storageService.getAvatarUrl(f.avatarUrl()),
                        f.addedAt()
                    );
                })
                .toList();
    }

    @Transactional
    public void sendRequest(CustomUserDetails userDetails, Long targetUserId) {
        Long currentUserId = userDetails.getId();

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        if (target.getStatus() == AccountStatus.BANNED)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Нельзя отправить заявку пользователю с заблокированным аккаунтом");

        if (currentUserId.equals(targetUserId)) {
            //такое с фронтенда не должно прийти, в теории может через какой нибудь
            // postman и туда выкинем 400 ошибку
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя добавить себя в друзья");
        }

        if (friendshipRepository.existsFriendshipBetween(currentUserId, targetUserId)) {
            throw new BadRequestException("Заявка уже отправлена");
        }
        Profile profile = profileRepository.findByUserId(currentUserId)
            .orElseThrow(() -> new EntityNotFoundException("Профиль не найден"));
        Friendship friendship = new Friendship(targetUserId, currentUserId, FriendshipStatus.PENDING);
        friendshipRepository.save(friendship);
        emailService.sendFriendRequestNotification(target.getEmail(), profile.getFirstName() + " " + profile.getLastName());
    }

    @Transactional
    public void acceptRequest(CustomUserDetails userDetails, Long fromUserId) {
        Long currentUserId = userDetails.getId();

        Friendship friendship = friendshipRepository.findByAddresseeIdAndRequesterId(currentUserId, fromUserId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена"));

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new BadRequestException("Заявка уже обработана");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void deleteFriend(CustomUserDetails userDetails, Long userId) {
        Long currentUserId = userDetails.getId();

        if (currentUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя удалить себя из друзей");
        }

        Friendship friendship = friendshipRepository
                .findFriendshipBetweenAndStatus(currentUserId, userId, FriendshipStatus.ACCEPTED)
                .orElseThrow(() -> new EntityNotFoundException("Друг не найден"));

        friendshipRepository.delete(friendship);
    }

    @Transactional
    public void rejectFriend(CustomUserDetails userDetails, Long userId) {
        Long currentUserId = userDetails.getId();

        if (currentUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Нельзя отклонить заявку от себя");
        }

        Friendship friendship = friendshipRepository
                .findFriendshipRequest(currentUserId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Друг не найден"));

        friendshipRepository.delete(friendship);
    }
}