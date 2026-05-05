package com.ysocial.org.ysocialsite.repository;

import com.ysocial.org.ysocialsite.dto.FriendDto;
import com.ysocial.org.ysocialsite.entites.Friendship;
import com.ysocial.org.ysocialsite.enums.FriendshipStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface FriendshipRepository extends ListCrudRepository<Friendship, Long> {


    @Query("""
        SELECT CASE 
            WHEN requester_id = :userId THEN addressee_id 
            ELSE requester_id 
        END 
        FROM friendships 
        WHERE status = :status 
        AND (requester_id = :userId OR addressee_id = :userId)
    """)
    List<Long> findFriendIdsWithStatus(@Param("userId") Long userId, @Param("status") FriendshipStatus status);


    @Query("""
    SELECT * FROM friendships 
    WHERE (requester_id = :user1Id AND addressee_id = :user2Id) 
    OR (requester_id = :user2Id AND addressee_id = :user1Id)
    """)
    Optional<Friendship> findFriendshipBetween(Long user1Id, Long user2Id);

    @Query("""
        SELECT COUNT(*) FROM friendships 
        WHERE (requester_id = :user1Id OR addressee_id = :user2Id) 
        AND status = :status
    """)
    Long countFriendshipByUsersAndStatus(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id, @Param("status") FriendshipStatus status);

    @Query("""
            SELECT 
                p.user_id as id, 
                concat(p.first_name, ' ', p.last_name) as name, 
                p.avatar_url as avatar_url, 
                f.created_at as added_at
            FROM friendships f
            JOIN profiles p ON f.requester_id = p.user_id
            WHERE f.addressee_id = :userId
            AND f.status = 'PENDING'
            ORDER BY f.created_at DESC
            """)
    List<FriendDto> findRequestsWithProfiles(@Param("userId") Long userId);

    @Query("""
        SELECT 
            p.user_id as id, 
            concat(p.first_name, ' ', p.last_name) as name, 
            p.avatar_url as avatar_url, 
            f.created_at as added_at
        FROM friendships f
        JOIN profiles p ON (
            (f.requester_id = p.user_id AND f.addressee_id = :userId) OR 
            (f.addressee_id = p.user_id AND f.requester_id = :userId)
        )
        WHERE (f.requester_id = :userId OR f.addressee_id = :userId)
        AND f.status = 'ACCEPTED'
        AND p.user_id != :userId
        ORDER BY f.created_at DESC
        """)
    List<FriendDto> findFriendsWithProfiles(@Param("userId") Long userId);
}
