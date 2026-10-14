package com.example.blogapi.service;

import com.example.blogapi.dto.PostRequest;
import com.example.blogapi.dto.PostResponse;
import com.example.blogapi.entity.Post;
import com.example.blogapi.entity.User;
import com.example.blogapi.repository.PostRepository;
import com.example.blogapi.repository.UserRepository;
import com.example.blogapi.specification.PostSpecification;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository,
            ModelMapper modelMapper) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found. Please log in again."
                ));
    }

    private Post findPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post not found: " + id
                ));
    }

    private void checkOwnership(Post post, User currentUser) {
        if (post.getAuthor() == null
                || !post.getAuthor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only modify your own stories."
            );
        }
    }

    private PostResponse toResponse(Post post) {
        PostResponse response =
                modelMapper.map(post, PostResponse.class);

        if (post.getAuthor() != null) {
            response.setAuthorId(post.getAuthor().getId());
            response.setAuthorName(post.getAuthor().getName());
        }

        return response;
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        return toResponse(findPostById(id));
    }

    @Transactional
    public PostResponse createPost(
            PostRequest request,
            String email) {

        User author = findUserByEmail(email);

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());
        post.setAuthor(author);

        Post savedPost = postRepository.save(post);

        return toResponse(savedPost);
    }

    @Transactional
    public PostResponse updatePost(
            Long id,
            PostRequest request,
            String email) {

        User currentUser = findUserByEmail(email);
        Post post = findPostById(id);

        checkOwnership(post, currentUser);

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());

        Post updatedPost = postRepository.save(post);

        return toResponse(updatedPost);
    }

    @Transactional
    public void deletePost(Long id, String email) {
        User currentUser = findUserByEmail(email);
        Post post = findPostById(id);

        checkOwnership(post, currentUser);

        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getMyStories(
            String email,
            Pageable pageable) {

        return postRepository
                .findByAuthor_EmailIgnoreCase(email, pageable)
                .map(this::toResponse);
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
                .map(this::toResponse);
    }
}
