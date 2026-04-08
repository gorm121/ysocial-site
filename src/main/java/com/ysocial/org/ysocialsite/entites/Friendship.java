package com.ysocial.org.ysocialsite.entites;

import com.ysocial.org.ysocialsite.enums.FriendshipStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


import java.time.LocalDateTime;


@Table("friendships")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Friendship {

    @Id
    private Long id;

    @Column("requester_id")
    private Long requesterId;

    @Column("addressee_id")
    private Long addresseeId;

    private FriendshipStatus status;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    public Friendship(Long addresseeId, Long requesterId, FriendshipStatus status) {
        this.addresseeId = addresseeId;
        this.requesterId = requesterId;
        this.status = status;
    }
}