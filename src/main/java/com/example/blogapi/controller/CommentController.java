
package com.example.blogapi.controller;

import com.example.blogapi.dto.CommentRequest;
import com.example.blogapi.dto.CommentResponse;
import com.example.blogapi.service.CommentService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public CommentResponse createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest request) {
        return commentService.createComment(postId, request);
    }

    @GetMapping
    public List<CommentResponse> getComments(
            @PathVariable Long postId) {
        return commentService.getCommentsByPostId(postId);
    }
}
