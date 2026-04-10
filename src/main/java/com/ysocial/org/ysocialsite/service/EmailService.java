package com.ysocial.org.ysocialsite.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("denis123984@yandex.ru");
        message.setTo(toEmail);
        message.setSubject("YSocial: Код подтверждения регистрации");
        message.setText("Добро пожаловать в YSocial!\n\n" +
                "Ваш код подтверждения: " + code + "\n\n" +
                "Введите этот код в приложении для завершения регистрации. " +
                "Код действителен в течение 15 минут.");

        try {
            mailSender.send(message);
            log.info("Письмо успешно отправлено на: {}", toEmail);
        } catch (Exception e) {
            log.error("Ошибка при отправке почты: {}", e.getMessage());
        }
    }
}

