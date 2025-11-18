package com.example.baseblock.board.repository;

import com.example.baseblock.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // (하드딜리트면 전체 조회)
    List<Post> findAll();

    // 닉네임으로 게시글 검색
    List<Post> findByAuthor_NicknameContaining(String nickname);

    // 작성자 id만 빠르게 조회 (권한 체크용, 선택)
    @Query("select p.author.id from Post p where p.id = :postId")
    Long findAuthorIdById(@Param("postId") Long postId);

    // 작성자 이메일 기반 빠른 존재 체크 (권한 체크용)
    boolean existsByIdAndAuthor_Email(Long id, String email);
}
