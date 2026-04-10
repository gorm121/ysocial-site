package com.ysocial.org.ysocialsite.service;


import com.ysocial.org.ysocialsite.dto.request.RegisterRequest;
import com.ysocial.org.ysocialsite.dto.request.VerifyRequest;
import com.ysocial.org.ysocialsite.entites.Profile;
import com.ysocial.org.ysocialsite.entites.User;
import com.ysocial.org.ysocialsite.enums.AccountStatus;
import com.ysocial.org.ysocialsite.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;


import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Slf4j
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
            log.info("Пароли не совпадают");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароли не совпадают");
        }

        if (userRepository.existsByEmailOrUsername(email, username)){
            log.info("Почта или никнейм уже существуют");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Почта или никнейм уже существуют");
        }
        String hashPassword = passwordEncoder.encode(request.getPassword());

        String code = generateCode();

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(hashPassword);
        user.setCode(code);
        user.setExpiryCode(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        log.info("Отправляем код на {}", email);
        emailService.sendVerificationCode(email, code);
    }

    @Transactional
    public void verifyCode(VerifyRequest request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Почта не найдена"));
        if (user.getStatus() == AccountStatus.ACTIVE) {
            throw new RuntimeException("Код истек, вернитесь на страницу регистрации и попробуйте снова");
//            throw new BadRequestException("Аккаунт уже активен");
        }

        if (user.getExpiryCode().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Код истек, вернитесь на страницу регистрации и попробуйте снова");
//            throw new BadRequestException("Код истек, вернитесь на страницу регистрации и попробуйте снова"");
        }

        String code = request.getCode();

        log.info("Код в базе {}, код в запросе {}", user.getCode(), code);

        if (!user.getCode().equals(code)) {
            log.error("Неверный код {}", code);
            throw new RuntimeException("Неверный код");
//            throw new BadRequestException("Неверный код");
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
