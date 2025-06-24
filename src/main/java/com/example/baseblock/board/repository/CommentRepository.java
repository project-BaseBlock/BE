package com.example.baseblock.board.repository;

import com.example.baseblock.board.entity.Comment;
import com.example.baseblock.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndDeletedAtIsNull(Long postId);
}
