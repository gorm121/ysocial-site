package com.ysocial.org.ysocialsite.dto;

public record ChatInListDto(
        Long chatId,
        Long userId,
        String name,
        String avatarUrl,
        String lastMessage
) {}