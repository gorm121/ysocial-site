package com.ysocial.org.ysocialsite.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;


@Table("profiles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Profile {

    @Id
    @Column("user_id")
    private Long userId;

    private String firstName;
    private String lastName;
    private String city;
    private LocalDate birthDate;
    private String bio;
    private String avatarUrl;

    @Builder.Default
    private boolean isPrivate = false;
}
