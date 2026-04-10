package com.ysocial.org.ysocialsite.controller;


import com.ysocial.org.ysocialsite.dto.request.RegisterRequest;
import com.ysocial.org.ysocialsite.dto.request.VerifyRequest;
import com.ysocial.org.ysocialsite.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes
    ) {
        log.info("Пришел запрос /register");
        if (bindingResult.hasErrors()) {
            log.info("Возникли проблемы");
            return "html/register";
        }

        try {
            authService.register(user);
            redirectAttributes.addAttribute("email", user.getEmail());
            return "redirect:/verify";
        } catch (Exception e) {
            log.info(e.getMessage());
            return "html/register";
        }
    }

    @GetMapping("/verify")
    public String showVerifyPage(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "html/verify";
    }

    @PostMapping("/verify")
    public String verifyCode(@RequestParam("email") String email,
                             @RequestParam("code") String code,
                             Model model,
                             HttpServletResponse response) {

        log.info("ЗАПРОС ПРИШЕЛ Email: {}, Code: {}", email, code);

        try {
            VerifyRequest request = new VerifyRequest();
            request.setCode(code);
            request.setEmail(email);
            authService.verifyCode(request);

            response.setHeader("HX-Redirect", "/login");
            return null;

        } catch (Exception ex) {
            log.error("Ошибка верификации: {}", ex.getMessage());
            response.setHeader("HX-Retarget", "body");
            response.setHeader("HX-Reswap", "beforeend");

            model.addAttribute("message", ex.getMessage());

            return "html/fragments :: error-toast";
        }
    }


    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "html/login";
    }
}