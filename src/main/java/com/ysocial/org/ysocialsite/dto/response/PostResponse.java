package com.ysocial.org.ysocialsite.dto.response;


import com.ysocial.org.ysocialsite.dto.ProfileShortDto;
import com.ysocial.org.ysocialsite.enums.ReactionType;
import lombok.*;



import java.util.List;


@Builder
public record PostResponse(
        Long id,
        ProfileShortDto author,
        String content,
        Long likesCount,
        Long dislikesCount,
        String imageUrl,
        ReactionType reactionType, // реакция смотрящего
        String createdAt,

        List<CommentResponse> comments
) { }
