package com.ysocial.org.ysocialsite.repository;



import com.ysocial.org.ysocialsite.entities.Profile;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ProfileRepository extends ListCrudRepository<Profile, Long>, PagingAndSortingRepository<Profile, Long> {
    
    Optional<Profile> findByUserId(Long userId);

    @Query("SELECT * FROM profiles WHERE user_id IN (:usersId)")
    List<Profile> findAllByUsersId(@Param("usersId") List<Long> usersId);
    @Query(
        value = """
            SELECT * FROM profiles 
            WHERE (:name IS NULL 
                  OR LOWER(first_name) LIKE LOWER(CONCAT('%', :name, '%'))
                  OR LOWER(last_name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:city IS NULL OR city = :city)
            AND (CAST(:birthDate AS date) IS NULL OR birth_date = CAST(:birthDate AS date))
            AND user_id != :currentUserId
            ORDER BY first_name
        """
    )
    List<Profile> searchProfilesList(@Param("currentUserId") Long currentUserId,
                                 @Param("name") String name,
                                 @Param("city") String city,
                                 @Param("birthDate") LocalDate birthDate);
}
