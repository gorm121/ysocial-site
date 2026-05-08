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
    @Transactional
    public void changeRole(UserDetails userDetails, UUID targetUserId, UserRole newRole) {
        User currentUser = getUserByUserDetails(userDetails);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (currentUser.getId().equals(target.getId())) {
            throw new RuntimeException("Вы не можете менять роль самому себе");
        }
        if (currentUser.getRole().ordinal() <= target.getRole().ordinal()) {
            throw new RuntimeException("Вы не можете менять роль пользователю с равным или более высоким статусом");
        }
        if (newRole == UserRole.SUPER_ADMIN && currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new RuntimeException("Только главный админ может назначать других главных админов");
        }

        target.setRole(newRole);
        userRepository.save(target); 
    }
}
