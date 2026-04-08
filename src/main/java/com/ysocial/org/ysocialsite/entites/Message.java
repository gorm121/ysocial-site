package com.ysocial.org.ysocialsite.entites;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


@Table("messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {

    @Id
    private Long id;

    @Column("chat_id")
    private Long chatId;

    @Column("sender_id")
    private Long senderId;

    @Column("recipient_id")
    private Long recipientId;

    private String content;

    @Builder.Default
    @Column("is_read")
    private boolean isRead = false;

    @CreatedDate
    @Column("sent_at")
    private LocalDateTime sentAt;
}