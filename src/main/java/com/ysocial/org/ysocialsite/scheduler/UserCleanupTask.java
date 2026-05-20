package com.ysocial.org.ysocialsite.scheduler;

import com.ysocial.org.ysocialsite.enums.AccountStatus;
import com.ysocial.org.ysocialsite.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class UserCleanupTask {

    private final UserRepository userRepository;

    public UserCleanupTask(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Запускается каждый час
    @Scheduled(cron = "0 0 * * * *")
    public void cleanUpUnverifiedUsers() {

        userRepository.deleteExpiredUnverifiedUsers(AccountStatus.PENDING_VERIFICATION, LocalDateTime.now());

        log.info("Очистка неактивированных аккаунтов выполнена: {}", LocalDateTime.now());
    }
}
