package com.ysocial.org.ysocialsite.repository;

import com.ysocial.org.ysocialsite.entites.Friendship;
import com.ysocial.org.ysocialsite.enums.FriendshipStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


import java.util.List;



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


}
