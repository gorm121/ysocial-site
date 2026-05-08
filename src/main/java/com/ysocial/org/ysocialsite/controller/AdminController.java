package org.ysocial.ru.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.ysocial.ru.enums.UserRole;
import org.ysocial.ru.service.UserService;

import java.util.UUID;


@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@Slf4j
public class AdminController {

    private final UserService userService;
    public AdminController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/{userId}/role")
    public String changeUserRole(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID userId,
            @RequestParam UserRole newRole
    ) {
        log.info("Получен запрос на изменение роли пользователя с ID {} на роль {}", userId, newRole);
        userService.changeRole(userDetails, userId, newRole);
        log.info("Роль пользователя с ID {} успешно изменена на {}", userId, newRole);
        return "redirect:/profiles/" + userId; 
    }


}