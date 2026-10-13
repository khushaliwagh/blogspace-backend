
package com.example.blogapi.repository;

import com.example.blogapi.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository
        extends JpaRepository<PostLike, Long> {

    boolean existsByPost_IdAndUser_Id(Long postId, Long userId);

    long countByPost_Id(Long postId);

    Optional<PostLike> findByPost_IdAndUser_Id(
            Long postId,
            Long userId
    );

    void deleteByPost_IdAndUser_Id(Long postId, Long userId);
}
