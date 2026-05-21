package com.ysocial.org.ysocialsite.repository;

import com.ysocial.org.ysocialsite.entities.Message;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends ListCrudRepository<Message, Long> {

    @Query("SELECT * FROM messages WHERE chat_id = :chatId")
    List<Message> findByChatId(@Param("chatId") Long chatId);
}
