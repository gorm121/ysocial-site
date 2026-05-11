package com.ysocial.org.ysocialsite.service;

import com.ysocial.org.ysocialsite.entites.User;
import com.ysocial.org.ysocialsite.enums.AccountStatus;
import com.ysocial.org.ysocialsite.enums.UserRole;
import com.ysocial.org.ysocialsite.exceptions.EntityNotFoundException;
import com.ysocial.org.ysocialsite.repository.UserRepository;
import com.ysocial.org.ysocialsite.security.CustomUserDetails;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByUserDetails(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    @Transactional
    public void changeRole(CustomUserDetails userDetails, Long targetUserId, UserRole newRole) {
        User currentUser = getUserByUserDetails(userDetails);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        if (currentUser.getId().equals(target.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вы не можете менять роль самому себе");
        }
        if (currentUser.getRole().ordinal() <= target.getRole().ordinal()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вы не можете менять роль пользователю с равным или более высоким статусом");
        }
        if (newRole == UserRole.SUPER_ADMIN && currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Только главный админ может назначать других главных админов");
        }

        target.setRole(newRole);
        userRepository.save(target); 
    }

    @Transactional
    public void banUser(CustomUserDetails userDetails, Long userId){
        userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Текущий пользователь не найден"));
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        target.setStatus(AccountStatus.BANNED);
        userRepository.save(target);
    }
}
