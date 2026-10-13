
package com.example.blogapi.service;

import com.example.blogapi.entity.Post;
import com.example.blogapi.entity.PostLike;
import com.example.blogapi.entity.User;
import com.example.blogapi.repository.PostLikeRepository;
import com.example.blogapi.repository.PostRepository;
import com.example.blogapi.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostLikeService(
            PostLikeRepository postLikeRepository,
            PostRepository postRepository,
            UserRepository userRepository) {

        this.postLikeRepository = postLikeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public boolean likePost(Long postId, Long userId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Post not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (postLikeRepository.existsByPost_IdAndUser_Id(
                postId, userId)) {
            return true;
        }

        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(user);

        postLikeRepository.save(postLike);

        return true;
    }

    @Transactional
    public boolean unlikePost(Long postId, Long userId) {

        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Post not found");
        }

        postLikeRepository.deleteByPost_IdAndUser_Id(
                postId, userId);

        return false;
    }

    @Transactional(readOnly = true)
    public long getLikeCount(Long postId) {

        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Post not found");
        }

        return postLikeRepository.countByPost_Id(postId);
    }

    @Transactional(readOnly = true)
    public boolean hasUserLiked(Long postId, Long userId) {

        return postLikeRepository.existsByPost_IdAndUser_Id(
                postId, userId);
    }
}
