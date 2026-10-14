
package com.example.blogapi.repository;

import com.example.blogapi.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface PostRepository
        extends JpaRepository<Post, Long>,
                JpaSpecificationExecutor<Post> {

    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Post> searchByTitle(@Param("keyword") String keyword);

    @Query("SELECT p FROM Post p WHERE p.category = :category")
    List<Post> findByCategory(@Param("category") String category);
    
    Page<Post> findByAuthor_EmailIgnoreCase(
            String email,
            Pageable pageable
    );
}
