package com.ysocial.org.ysocialsite.entites;


import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Table("posts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Post {

    @Id
    private Long id;

    @Column("author_id")
    private Long authorId;

    private String content;

    @Column("image_url")
    private String imageUrl;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @Builder.Default
    @MappedCollection(idColumn = "post_id")
    private Set<Comment> comments = new LinkedHashSet<>();

    @Builder.Default
    @MappedCollection(idColumn = "post_id")
    private Set<PostReaction> reactions = new LinkedHashSet<>();
}
