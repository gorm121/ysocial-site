package com.ysocial.org.ysocialsite.dto;

import com.ysocial.org.ysocialsite.entities.Message;

public record MessageInChatDto(
        Long senderId,
        Long recipientId,
        String text,
        String avatarUrl,
        String currentUserAvatarUrl,
        String sentAt
) {
    public MessageInChatDto(Message message, String avatarUrl, String currentUserAvatarUrl, String sentAt){
        this(
                message.getSenderId(),
                message.getRecipientId(),
                message.getContent(),
                avatarUrl,
                currentUserAvatarUrl,
                sentAt
        );
    }
}
