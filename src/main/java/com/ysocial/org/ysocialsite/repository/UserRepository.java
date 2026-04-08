package com.ysocial.org.ysocialsite.repository;

import com.ysocial.org.ysocialsite.entites.User;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends ListCrudRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
