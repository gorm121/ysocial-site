package com.ysocial.org.ysocialsite.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationCode(String toEmail, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

//        message.setFrom("denis123984@yandex.ru");
//        message.setTo(toEmail);
//        message.setSubject("YSocial: Код подтверждения регистрации");
//        message.setText("Добро пожаловать в YSocial!\n\n" +
//                "Ваш код подтверждения: " + code + "\n\n" +
//                "Введите этот код в приложении для завершения регистрации. " +
//                "Код действителен в течение 15 минут.");

        mailMessage.setFrom("denis123984@yandex.ru");
        mailMessage.setTo(toEmail);
        mailMessage.setSubject("YSocial: Регистрация");
        mailMessage.setText(message);

        try {
            mailSender.send(mailMessage);
        } catch (Exception e) {
            // нет смысла кидать исключение так как это фоновый поток, а основной поток
            // уже закончил работу
            log.error("Ошибка при отправке email на {}: {}", toEmail, e.getMessage());
        }
    }
}

