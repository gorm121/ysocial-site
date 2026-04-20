package com.ysocial.org.ysocialsite.service;


import com.ysocial.org.ysocialsite.entites.User;
import com.ysocial.org.ysocialsite.repository.UserRepository;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByUserDetails(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}
