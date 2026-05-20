package com.ysocial.org.ysocialsite.entities;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("chats")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Chat {
    
    @Id
    private Long id;

    @Column("user1")
    private Long user1;

    @Column("user2")
    private Long user2;

    private String lastMessageText;

    @CreatedDate
    private LocalDateTime lastSent = LocalDateTime.now();
}
