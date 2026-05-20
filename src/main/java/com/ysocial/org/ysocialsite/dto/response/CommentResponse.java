package com.ysocial.org.ysocialsite.dto.response;

import com.ysocial.org.ysocialsite.dto.ProfileShortDto;
import com.ysocial.org.ysocialsite.entities.Comment;
import lombok.*;

@Builder
public record CommentResponse(
        Long id,
        ProfileShortDto author,
        String text) {


    public  CommentResponse(Comment comment, ProfileShortDto author) {
        this(comment.getId(), author, comment.getText());
    }
}
