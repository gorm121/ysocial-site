package com.ysocial.org.ysocialsite.entites;

import com.ysocial.org.ysocialsite.enums.AccountStatus;
import com.ysocial.org.ysocialsite.enums.UserRole;

import lombok.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


@Table("users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private Long id;

    private String username;
    
    private String email;

    @Column("password_hash")
    private String passwordHash;

    @Builder.Default
    private UserRole role = UserRole.USER;

    @Builder.Default
    private AccountStatus status = AccountStatus.PENDING_VERIFICATION;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    private String code;

    @Column("expiry_code")
    private LocalDateTime expiryCode;

    @MappedCollection(idColumn = "user_id")
    private Profile profile;
}
