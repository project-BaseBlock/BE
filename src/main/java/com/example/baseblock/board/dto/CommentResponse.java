package com.example.baseblock.board.dto;

import com.example.baseblock.board.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class CommentResponse {

    private Long id;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private String authorEmail;

    public static CommentResponse fromEntity(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(comment.getAuthor().getNickname())
                .authorEmail(comment.getAuthor().getEmail())
                .createdAt(comment.getCreatedAt())
                .build();
    }

}
