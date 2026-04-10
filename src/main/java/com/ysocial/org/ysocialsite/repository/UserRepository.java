package com.ysocial.org.ysocialsite.repository;

import com.ysocial.org.ysocialsite.entites.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends ListCrudRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("SELECT COUNT(*) > 0 FROM users WHERE email = :email OR username = :username")
    boolean existsByEmailOrUsername(@Param("email") String email, @Param("username") String username);
}
