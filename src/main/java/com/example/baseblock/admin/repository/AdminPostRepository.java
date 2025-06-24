package com.example.baseblock.admin.repository;

import com.example.baseblock.board.entity.Post;
import com.example.baseblock.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminPostRepository extends JpaRepository<Post, Long> {

    List<Post> findByAuthor(User user); // 유저기준으로 검색
    List<Post> findByAuthorNicknameContaining(String keyword);

}
