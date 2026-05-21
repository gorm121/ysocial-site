package com.ysocial.org.ysocialsite.repository;

import com.ysocial.org.ysocialsite.entities.User;
import com.ysocial.org.ysocialsite.enums.AccountStatus;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends ListCrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // по дефолту любой кастомный запрос в спринге воспринимается как запрос на чтение (SELECT),
    // под капотом спринг его выполняет через executeQuery(), который ожидает какойто ResultSet,
    // но запросы которые изменяют состояние базы ничего не возвращают,
    // поэтому здесь вешаем аннотицию @Modifying, и спринг понимает что запрос нужно выполнить через executeUpdate()
    @Modifying
    @Query("DELETE FROM users WHERE status = :status AND expiry_code < :now")
    void deleteExpiredUnverifiedUsers(@Param("status") AccountStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(*) > 0 FROM users WHERE email = :email OR username = :username")
    boolean existsByEmailOrUsername(@Param("email") String email, @Param("username") String username);

    Optional<User> findByCode(String code);
}
