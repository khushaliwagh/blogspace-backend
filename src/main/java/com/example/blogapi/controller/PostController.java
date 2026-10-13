
package com.example.blogapi.controller;

import com.example.blogapi.dto.PostRequest;
import com.example.blogapi.dto.PostResponse;
import com.example.blogapi.service.PostService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public Page<PostResponse> getAllPosts(
            @PageableDefault(size = 10) Pageable pageable) {
        return postService.getAllPosts(pageable);
    }

    @GetMapping("/{id}")
    public PostResponse getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    @PostMapping
    public PostResponse createPost(@RequestBody PostRequest request) {
        return postService.createPost(request);
    }

    @PutMapping("/{id}")
    public PostResponse updatePost(
            @PathVariable Long id,
            @RequestBody PostRequest request) {
        return postService.updatePost(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        postService.deletePost(id);
    }

    @GetMapping("/search")
    public Page<PostResponse> searchPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 10) Pageable pageable) {
        return postService.searchPosts(title, category, pageable);
    }
}
