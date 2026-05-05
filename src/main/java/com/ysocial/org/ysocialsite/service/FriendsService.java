package com.ysocial.org.ysocialsite.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ysocial.org.ysocialsite.dto.FriendDto;
import com.ysocial.org.ysocialsite.entites.User;
import com.ysocial.org.ysocialsite.repository.FriendshipRepository;
import com.ysocial.org.ysocialsite.repository.UserRepository;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@Service

@Slf4j
public class FriendsService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    private final UserService userService;

    public FriendsService(FriendshipRepository friendshipRepository,
         UserRepository userRepository,
         UserService userService){
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.userService = userService;
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
}