package com.ysocial.org.ysocialsite.repository;

import com.ysocial.org.ysocialsite.entites.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;


public interface PostRepository extends ListCrudRepository<Post, Long>, PagingAndSortingRepository<Post, Long> {

    @Query("""
        SELECT COUNT(p.id) 
        FROM posts p 
        JOIN users u ON p.author_id = u.id 
        WHERE p.author_id IN (:friendIds) AND u.status != 'BANNED'
        """)
    long countNewsFeed(@Param("friendIds") List<Long> friendIds);

    @Query("""
        SELECT p.id 
        FROM posts p 
        JOIN users u ON p.author_id = u.id 
        WHERE p.author_id IN (:friendIds) AND u.status != 'BANNED' 
        ORDER BY p.created_at DESC, p.id DESC 
        LIMIT :limit OFFSET :offset
        """)
    List<Long> findNewsFeedIds(@Param("friendIds") List<Long> friendIds, @Param("limit") int limit, @Param("offset") long offset);

    @Query("SELECT * FROM posts WHERE id IN (:ids) ORDER BY created_at DESC, id DESC")
    List<Post> findAllByIdsSorted(@Param("ids") List<Long> ids);

    Page<Post> findByAuthorId(Long authorId, Pageable pageable);

}