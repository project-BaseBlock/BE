package com.example.baseblock.board.service;

import com.example.baseblock.board.dto.PostCreateRequest;
import com.example.baseblock.board.dto.PostResponse;
import com.example.baseblock.board.dto.PostUpdateRequest;
import com.example.baseblock.board.entity.Post;
import com.example.baseblock.board.repository.PostRepository;
import com.example.baseblock.user.entity.User;
import com.example.baseblock.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<PostResponse> findAll() {
        return postRepository.findAll().stream()
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public PostResponse findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
        return PostResponse.fromEntity(post);
    }

    //게시글 작성
    public void createPost(PostCreateRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(user)                   // 여기서 user는 변수명이다.
                .build();

        postRepository.save(post);
    }

    public boolean isPostAuthor(Long postId, String email) {
        return postRepository.findById(postId)
                .map(post -> post.getAuthor().getEmail().equals(email))
                .orElse(false);
    }

    //게시글 수정
    @Transactional
    public void updatePost(Long postId, PostUpdateRequest request, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 작성자 본인 확인
        if (!post.getAuthor().getEmail().equals(email)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        post.update(request.getTitle(), request.getContent());
    }

    //게시글 삭제
    @Transactional
    public void deletePost(Long postId, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

        if (!post.getAuthor().getEmail().equals(email)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }

}
