package com.example.baseblock.admin.controller;

import com.example.baseblock.admin.dto.AdminPostResponse;
import com.example.baseblock.admin.service.AdminPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final AdminPostService adminPostService;

    // 1. 전체 글 조회
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public List<AdminPostResponse> getAllPosts() {
        return adminPostService.getAllPosts();
    }

    // 2. 닉네임으로 검색
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public List<AdminPostResponse> searchByNickname(@RequestParam String nickname) {
        return adminPostService.getPostsByNickname(nickname);
    }

    // 3. 글 삭제
    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public void deletePost(@PathVariable Long postId) {
        adminPostService.deletePost(postId);
    }

}
