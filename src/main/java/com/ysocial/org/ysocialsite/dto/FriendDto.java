package com.ysocial.org.ysocialsite.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FriendDto(
    Long id,
    String name, 
    String avatarUrl, 
    LocalDateTime addedAt
) {
    public String getFormattedDate() {
        if (addedAt == null) return "";
        return addedAt.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM, HH:mm"));
    }
}
