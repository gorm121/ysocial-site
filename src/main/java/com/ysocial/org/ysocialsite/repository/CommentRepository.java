package com.ysocial.org.ysocialsite.repository;

import com.ysocial.org.ysocialsite.entities.Comment;
import org.springframework.data.repository.ListCrudRepository;

public interface CommentRepository extends ListCrudRepository<Comment, Long> {
}
