package com.ysocial.org.ysocialsite.repository;

import com.ysocial.org.ysocialsite.entites.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends ListCrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);


    @Query("SELECT COUNT(*) > 0 FROM users WHERE email = :email OR username = :username")
    boolean existsByEmailOrUsername(@Param("email") String email, @Param("username") String username);
}
