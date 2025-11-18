package com.example.baseblock.board.controller;

import com.example.baseblock.board.dto.CommentRequest;
import com.example.baseblock.board.dto.CommentResponse;
import com.example.baseblock.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    // 게시글 별 댓글 조회
    @GetMapping("/{postId}")
    public List<CommentResponse> getComments(@PathVariable Long postId) {
        return commentService.getCommentsByPostId(postId);
    }

    // 댓글 작성
    @PostMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public Long createComment(@RequestBody CommentRequest request, Authentication authentication) {
        String email = authentication.getName(); // 로그인한 유저 email
        return commentService.createComment(request, email);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    @PreAuthorize("@commentService.isCommentAuthor(#commentId, authentication.name)")
    public void deleteComment(@PathVariable Long commentId, Authentication authentication) {
        String email = authentication.getName();
        commentService.deleteComment(commentId, email);
    }
}
