package com.ysocial.org.ysocialsite.service;


import com.ysocial.org.ysocialsite.dto.request.RegisterRequest;
import com.ysocial.org.ysocialsite.dto.request.VerifyRequest;
import com.ysocial.org.ysocialsite.entities.Profile;
import com.ysocial.org.ysocialsite.entities.User;
import com.ysocial.org.ysocialsite.enums.AccountStatus;
import com.ysocial.org.ysocialsite.exceptions.AccountActivateException;
import com.ysocial.org.ysocialsite.exceptions.BadRequestException;
import com.ysocial.org.ysocialsite.exceptions.EntityNotFoundException;
import com.ysocial.org.ysocialsite.repository.UserRepository;


import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String username = request.getUsername();
        String email = request.getEmail();

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Пароли не совпадают");
        }

        if (userRepository.existsByEmailOrUsername(email, username)){
            throw new BadRequestException("Почта или никнейм уже существуют");
        }
        String hashPassword = passwordEncoder.encode(request.getPassword());

        String code = UUID.randomUUID().toString();

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(hashPassword);
        user.setCode(code);
        user.setExpiryCode(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        String message = String.format(
                """
                Привет, %s!\s
                Добро пожаловать. Для активации аккаунта перейди по ссылке:\s
                http://localhost:8081/activate/%s
                """,
                user.getUsername(),
                user.getCode()
        );

        emailService.sendSimpleMessage(email, "Активация аккаунта" ,message);
    }

    @Transactional
    public void activateAccount(String code) {
        User user = userRepository.findByCode(code)
                .orElseThrow(() -> new AccountActivateException("Неверный код активации"));

        if (user.getStatus() == AccountStatus.ACTIVE) {
            throw new AccountActivateException("Аккаунт уже активен");
        }

        if (user.getExpiryCode().isBefore(LocalDateTime.now())) {
            throw new AccountActivateException("Код истек, вернитесь на страницу регистрации и попробуйте снова");
        }

        Profile profile = new Profile();
        profile.setUserId(user.getId());
        profile.setFirstName(user.getUsername());
        profile.setLastName("");

        user.setProfile(profile);
        user.setExpiryCode(null);
        user.setCode(null);
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void verifyCode(VerifyRequest request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Произошла ошибка. Попробуйте пройти регистрацию заново."));
        if (user.getStatus() == AccountStatus.ACTIVE) {
            throw new BadRequestException("Аккаунт уже активен");
        }

        if (user.getExpiryCode().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Код истек, вернитесь на страницу регистрации и попробуйте снова");
        }

        String code = request.getCode();

        if (!user.getCode().equals(code)) {
            throw new BadRequestException("Неверный код");
        }

        Profile profile = new Profile();
        profile.setUserId(user.getId());
        profile.setFirstName(user.getUsername());
        profile.setLastName("");

        user.setProfile(profile);
        user.setExpiryCode(null);
        user.setCode(null);
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
    }

    public String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 1000 + random.nextInt(9000);
        return String.valueOf(code);
    }
}
