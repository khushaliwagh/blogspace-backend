
package com.example.blogapi.controller;

import com.example.blogapi.entity.User;
import com.example.blogapi.repository.UserRepository;
import com.example.blogapi.service.PostLikeService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}")
public class PostLikeController {

    private final PostLikeService postLikeService;
    private final UserRepository userRepository;

    public PostLikeController(
            PostLikeService postLikeService,
            UserRepository userRepository) {
        this.postLikeService = postLikeService;
        this.userRepository = userRepository;
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Please log in");
        }

        User user = userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        return user.getId();
    }

    @GetMapping("/likes")
    public Map<String, Object> getLikeCount(
            @PathVariable Long postId) {

        return Map.of(
                "postId", postId,
                "likeCount", postLikeService.getLikeCount(postId)
        );
    }

    @GetMapping("/likes/me")
    public Map<String, Object> getMyLikeStatus(
            @PathVariable Long postId,
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        return Map.of(
                "postId", postId,
                "liked", postLikeService.hasUserLiked(postId, userId)
        );
    }

    @PostMapping("/like")
    public Map<String, Object> likePost(
            @PathVariable Long postId,
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);
        postLikeService.likePost(postId, userId);

        return Map.of(
                "postId", postId,
                "liked", true,
                "likeCount", postLikeService.getLikeCount(postId)
        );
    }

    @DeleteMapping("/like")
    public Map<String, Object> unlikePost(
            @PathVariable Long postId,
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);
        postLikeService.unlikePost(postId, userId);

        return Map.of(
                "postId", postId,
                "liked", false,
                "likeCount", postLikeService.getLikeCount(postId)
        );
    }
}
