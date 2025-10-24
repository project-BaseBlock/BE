package com.example.baseblock.board.controller;

import com.example.baseblock.board.dto.PostCreateRequest;
import com.example.baseblock.board.dto.PostResponse;
import com.example.baseblock.board.dto.PostUpdateRequest;
import com.example.baseblock.board.service.PostService;
import lombok.RequiredArgsConstructor;
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

    // 목록 (공개)
    @GetMapping
    public List<PostResponse> getAllPosts() {
        return postService.findAll();
    }

    // 단건 (공개)
    @GetMapping("/{id}")
    public PostResponse getPostById(@PathVariable Long id) {
        return postService.findById(id);
    }

    // 작성 (USER/ADMIN/MASTER)
    @PostMapping("/new")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MASTER')")
    public ResponseEntity<Void> createPost(@RequestBody PostCreateRequest request,
                                           Authentication authentication) {
        String email = authentication.getName();
        postService.createPost(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 수정: 작성자 or ADMIN/MASTER
    @PutMapping("/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN','MASTER') or @postService.isPostAuthor(#postId, authentication.name)")
    public ResponseEntity<Void> updatePost(@PathVariable Long postId,
                                           @RequestBody PostUpdateRequest request,
                                           Authentication authentication) {
        String email = authentication.getName();
        postService.updatePost(postId, request, email);
        return ResponseEntity.ok().build();
    }

    // 삭제: 작성자 or ADMIN/MASTER (하드딜리트)
    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN','MASTER') or @postService.isPostAuthor(#postId, authentication.name)")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId,
                                           Authentication authentication) {
        String email = authentication.getName();
        postService.deletePost(postId, email);
        return ResponseEntity.noContent().build();
    }
}
