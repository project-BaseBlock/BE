package com.example.baseblock.board.service;

import com.example.baseblock.board.dto.CommentRequest;
import com.example.baseblock.board.dto.CommentResponse;
import com.example.baseblock.board.entity.Comment;
import com.example.baseblock.board.entity.Post;
import com.example.baseblock.board.repository.CommentRepository;
import com.example.baseblock.board.repository.PostRepository;
import com.example.baseblock.user.entity.User;
import com.example.baseblock.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글에 달린 댓글 목록 조회 (비로그인 허용)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdAndDeletedAtIsNull(postId).stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }
    // 댓글 등록
    @Transactional
    public Long createComment(CommentRequest request, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("유저없음"));
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .author(user)
                .createdAt(LocalDateTime.now())
                .build();

        commentRepository.save(comment);

        return comment.getId();

    }

    public boolean isCommentAuthor(Long commentId, String email) {
        return commentRepository.findById(commentId)
                .map(comment -> comment.getAuthor().getEmail().equals(email))
                .orElse(false);
    }


    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음"));

        if (!comment.getAuthor().getEmail().equals(userEmail)) {
            throw new SecurityException("삭제 권한 없음");
        }

        commentRepository.delete(comment);
    }

}
