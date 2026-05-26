package com.ysocial.org.ysocialsite.controller;


import com.ysocial.org.ysocialsite.dto.request.RegisterRequest;
import com.ysocial.org.ysocialsite.dto.request.VerifyRequest;
import com.ysocial.org.ysocialsite.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "html/register";
    }


    @PostMapping("/register")
    public String registerUser( @Valid @ModelAttribute RegisterRequest user,
                                RedirectAttributes redirectAttributes,
                                HttpServletResponse response
    ) {
        authService.register(user);
        return "registration-success";
    }

    @GetMapping("/activate/{code}")
    public String activateAccount(@PathVariable String code){
        authService.activateAccount(code);
        return "activation-success";
    }


    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "login";
    }
}