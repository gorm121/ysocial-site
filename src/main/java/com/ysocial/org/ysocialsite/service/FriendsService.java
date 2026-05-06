package com.ysocial.org.ysocialsite.service;


import com.ysocial.org.ysocialsite.entites.Friendship;
import com.ysocial.org.ysocialsite.enums.AccountStatus;
import com.ysocial.org.ysocialsite.enums.FriendshipStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ysocial.org.ysocialsite.dto.FriendDto;
import com.ysocial.org.ysocialsite.entites.User;
import com.ysocial.org.ysocialsite.repository.FriendshipRepository;
import com.ysocial.org.ysocialsite.repository.UserRepository;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FriendsService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendsService(FriendshipRepository friendshipRepository,
                          UserRepository userRepository)
    {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<FriendDto> getRequests(CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        
        return friendshipRepository.findRequestsWithProfiles(userId)
            .stream().map( f -> 
                new FriendDto(
                    f.id(), 
                    f.name(), 
                    null,
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
                        null,
                        f.addedAt()
                    );
                })
                .toList();
    }

    @Transactional
    public void sendRequest(CustomUserDetails userDetails, Long targetUserId) {
        Long currentUserId = userDetails.getId();

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (target.getStatus() == AccountStatus.BANNED)
            throw new RuntimeException("Нельзя отправить заявку пользователю с заблокированным аккаунтом");

        if (currentUserId.equals(targetUserId)) {
            //такое с фронтенда не должно прийти, в теории может через какой нибудь
            // postman и туда выкинем 400 ошибку
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя добавить себя в друзья");
        }

        if (!userRepository.existsById(targetUserId)) {
            throw new RuntimeException("Не найден пользователь для отправки заявки");
        }

        if (friendshipRepository.existsFriendshipBetween(currentUserId, targetUserId)) {
            throw new RuntimeException("Заявка уже отправлена");
        }

        Friendship friendship = new Friendship(targetUserId, currentUserId, FriendshipStatus.PENDING);
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void acceptRequest(CustomUserDetails userDetails, Long fromUserId) {
        Long currentUserId = userDetails.getId();

        Friendship friendship = friendshipRepository.findByAddresseeIdAndRequesterId(currentUserId, fromUserId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("Заявка уже обработана");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void deleteFriend(CustomUserDetails userDetails, Long userId) {
        Long currentUserId = userDetails.getId();

        if (currentUserId.equals(userId)) {
            throw new RuntimeException("Нельзя удалить себя из друзей");
        }

        Friendship friendship = friendshipRepository
                .findFriendshipBetweenAndStatus(currentUserId, userId, FriendshipStatus.ACCEPTED)
                .orElseThrow(() -> new RuntimeException("Друг не найден"));

        friendshipRepository.delete(friendship);
    }

    @Transactional
    public void rejectFriend(CustomUserDetails userDetails, Long userId) {
        Long currentUserId = userDetails.getId();

        if (currentUserId.equals(userId)) {
            throw new RuntimeException("Нельзя удалить себя из друзей");
        }

        Friendship friendship = friendshipRepository
                .findFriendshipRequest(currentUserId, userId)
                .orElseThrow(() -> new RuntimeException("Друг не найден"));

        friendshipRepository.delete(friendship);
    }
}