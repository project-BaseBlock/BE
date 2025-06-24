package com.example.baseblock.board.repository;

import com.example.baseblock.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 모든 게시글 조회 (삭제되지 않은 글만)
    List<Post> findByDeletedAtIsNull();

    // 닉네임으로 게시글 검색
    List<Post> findByAuthor_NicknameContainingAndDeletedAtIsNull(String nickname);

}
