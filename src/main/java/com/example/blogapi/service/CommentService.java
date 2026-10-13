
package com.example.blogapi.service;

import com.example.blogapi.dto.CommentRequest;
import com.example.blogapi.dto.CommentResponse;
import com.example.blogapi.entity.Comment;
import com.example.blogapi.entity.Post;
import com.example.blogapi.repository.CommentRepository;
import com.example.blogapi.repository.PostRepository;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    public CommentService(
            CommentRepository commentRepository,
            PostRepository postRepository,
            ModelMapper modelMapper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public CommentResponse createComment(
            Long postId, CommentRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                    new RuntimeException("Post not found: " + postId));

        Comment comment = new Comment();
        comment.setMessage(request.getMessage());
        comment.setPost(post);

        Comment saved = commentRepository.save(comment);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {

        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found: " + postId);
        }

        return commentRepository.findByPostId(postId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CommentResponse toResponse(Comment comment) {
        CommentResponse response =
                modelMapper.map(comment, CommentResponse.class);

        response.setPostId(comment.getPost().getId());

        return response;
    }
}
