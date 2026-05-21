package com.ysocial.org.ysocialsite.dto;

import java.util.List;

public record ChatDto(
        Long chatId,
        Long userId,
        String name,
        String avatarUrl,
        List<MessageInChatDto> messages
) {}