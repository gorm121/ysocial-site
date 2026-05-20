package com.ysocial.org.ysocialsite.controller;


import com.ysocial.org.ysocialsite.dto.request.RegisterRequest;
import com.ysocial.org.ysocialsite.dto.request.VerifyRequest;
import com.ysocial.org.ysocialsite.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    )  {
        authService.register(user);
        redirectAttributes.addAttribute("email", user.getEmail());
        response.setHeader("HX-Redirect", "/verify?email=" + user.getEmail());
        return null;
    }

    @GetMapping("/verify")
    public String showVerifyPage(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "html/verify";
    }

    @PostMapping("/verify")
    public String verifyCode(@RequestParam("email") String email,
                             @RequestParam("code") String code,
                             HttpServletResponse response) {

        VerifyRequest request = new VerifyRequest();
        request.setCode(code);
        request.setEmail(email);
        authService.verifyCode(request);

        response.setHeader("HX-Redirect", "/login");
        return null;
    }


    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "html/login";
    }
}