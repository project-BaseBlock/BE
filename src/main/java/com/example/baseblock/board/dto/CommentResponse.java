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

    public static CommentResponse fromEntity(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(comment.getAuthor().getNickname())
                .createdAt(comment.getCreatedAt())
                .build();
    }

}
