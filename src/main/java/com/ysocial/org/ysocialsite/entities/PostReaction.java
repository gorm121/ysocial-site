package com.ysocial.org.ysocialsite.entities;

import com.ysocial.org.ysocialsite.enums.ReactionType;

import lombok.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


@Table("post_reactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostReaction {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    private ReactionType type;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
}