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
    private final String fromEmail = "denis123984@yandex.ru";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    @Async
    public void sendSimpleMessage(String toEmail, String subject, String text) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);

        try {
            mailSender.send(mailMessage);
            log.info("Письмо успешно отправлено на {}", toEmail);
        } catch (Exception e) {
            log.error("Ошибка при отправке email на {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendFriendRequestNotification(String toEmail, String requesterName) {
        String subject = "YSocial: Новая заявка в друзья";
        String text = String.format("Привет! Пользователь %s хочет добавить вас в друзья. " +
                "Посмотрите вашу страницу заявок на сайте.", requesterName);

        sendSimpleMessage(toEmail, subject, text); 
    }

    @Async
    public void sendNewMessageNotification(String toEmail, String senderName) {
        String subject = "YSocial: Новое личное сообщение";
        String text = String.format("Привет! Пользователь %s отправил вам личное сообщение. " +
                "Прочитайте его в разделе диалогов.", senderName);

        sendSimpleMessage(toEmail, subject, text);
    }
}