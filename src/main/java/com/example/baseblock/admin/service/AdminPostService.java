package com.example.baseblock.admin.service;

import com.example.baseblock.admin.dto.AdminPostResponse;
import com.example.baseblock.admin.repository.AdminPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AdminPostService {

    private final AdminPostRepository adminPostRepository;

    // 전체 글 조회
    public List<AdminPostResponse> getAllPosts() {
        return adminPostRepository.findAll().stream()
                .map(AdminPostResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 닉네임으로 글 검색
    public List<AdminPostResponse> getPostsByNickname(String nickname) {
        return adminPostRepository.findByAuthorNicknameContaining(nickname).stream()
                .map(AdminPostResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 게시글 삭제
    public void deletePost(Long postId) {
        adminPostRepository.deleteById(postId);
    }

}
