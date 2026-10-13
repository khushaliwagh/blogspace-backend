
package com.example.blogapi.service;

import com.example.blogapi.dto.PostRequest;
import com.example.blogapi.dto.PostResponse;
import com.example.blogapi.entity.Post;
import com.example.blogapi.repository.PostRepository;
import com.example.blogapi.specification.PostSpecification;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    public PostService(
            PostRepository postRepository,
            ModelMapper modelMapper) {
        this.postRepository = postRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(post -> modelMapper.map(post, PostResponse.class));
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Post not found: " + id));

        return modelMapper.map(post, PostResponse.class);
    }

    @Transactional
    public PostResponse createPost(PostRequest request) {
        Post post = modelMapper.map(request, Post.class);
        Post savedPost = postRepository.save(post);

        return modelMapper.map(savedPost, PostResponse.class);
    }

    @Transactional
    public PostResponse updatePost(Long id, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Post not found: " + id));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());

        Post updatedPost = postRepository.save(post);

        return modelMapper.map(updatedPost, PostResponse.class);
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Post not found: " + id));

        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(
            String title,
            String category,
            Pageable pageable) {

        Specification<Post> spec = Specification
                .where(PostSpecification.titleContains(title))
                .and(PostSpecification.categoryEquals(category));

        return postRepository.findAll(spec, pageable)
                .map(post -> modelMapper.map(post, PostResponse.class));
    }
}
