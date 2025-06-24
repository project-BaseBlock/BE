package com.example.baseblock.board.controller;

import com.example.baseblock.board.dto.PostCreateRequest;
import com.example.baseblock.board.dto.PostResponse;
import com.example.baseblock.board.dto.PostUpdateRequest;
import com.example.baseblock.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    //게시글 조회
    @GetMapping
    public List<PostResponse> getAllPosts() {
        return postService.findAll();
    }
    //게시글 검색
    @GetMapping("/{id}")
    public PostResponse getPostById(@PathVariable Long id) {
        return postService.findById(id);
    }

    //게시글 작성
    @PostMapping("/posts")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<Void> createPost(@RequestBody PostCreateRequest request, Authentication authentication) {
        String email = authentication.getName(); // 로그인한 사용자 이메일
        postService.createPost(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //게시글 수정
    @PutMapping("/{postId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<Void> updatePost(@PathVariable Long postId,
                                           @RequestBody PostUpdateRequest request,
                                           Authentication authentication) {
        String email = authentication.getName();
        postService.updatePost(postId, request, email);
        return ResponseEntity.ok().build();
    }

    //게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName(); // 현재 로그인한 유저
        postService.deletePost(id, email);
        return ResponseEntity.noContent().build();
    }

}
