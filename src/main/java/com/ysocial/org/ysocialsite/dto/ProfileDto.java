package com.ysocial.org.ysocialsite.dto;

import com.ysocial.org.ysocialsite.enums.FriendshipStatus;
import com.ysocial.org.ysocialsite.enums.UserRole;
import lombok.Builder;



@Builder
public record ProfileDto(
    Long id,
    String firstName, 
    String lastName, 
    String bio, 
    String city, 
    String birthDate,
    String avatarUrl, 
    Long countFriends,
    boolean isOwnProfile,
    FriendshipStatus friendStatus,
    UserRole userRole,
    boolean privateProfile
) {}
