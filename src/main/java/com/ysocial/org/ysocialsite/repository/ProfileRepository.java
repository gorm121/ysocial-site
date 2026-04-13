package com.ysocial.org.ysocialsite.repository;



import com.ysocial.org.ysocialsite.entites.Profile;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;


public interface ProfileRepository extends ListCrudRepository<Profile, Long>, PagingAndSortingRepository<Profile, Long> {
    
    Optional<Profile> findByUserId(Long userId);

    @Query("SELECT * FROM profiles WHERE user_id IN (:usersId)")
    List<Profile> findAllByUsersId(@Param("usersId") List<Long> usersId);

}