package com.ysocial.org.ysocialsite.repository;

import com.ysocial.org.ysocialsite.entities.Chat;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends ListCrudRepository<Chat, Long> {

    @Query("SELECT * FROM chats WHERE" +
            "(user1 = :user1 AND user2 = :user2) OR" +
            "(user1 = :user2 AND user2 = :user1)"
    )
    Optional<Chat> findChat(@Param("user1") Long user1, @Param("user2") Long user2);

    @Query("SELECT * FROM chats WHERE user1 = :userId OR user2 = :userId")
    List<Chat> findAllByUser(@Param("userId") Long userId);
}