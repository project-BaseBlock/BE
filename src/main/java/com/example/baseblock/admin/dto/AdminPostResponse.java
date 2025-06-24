package com.example.baseblock.admin.dto;

import com.example.baseblock.board.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminPostResponse {

    private final Long id;
    private final String title;
    private final String authorEmail;
    private final String authorNickname;
    private final LocalDateTime createdAt;

    private AdminPostResponse(Long id, String title, String authorEmail, String authorNickname, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.authorEmail = authorEmail;
        this.authorNickname = authorNickname;
        this.createdAt = createdAt;
    }

    public static AdminPostResponse fromEntity(Post post) {
        return new AdminPostResponse(
                post.getId(),
                post.getTitle(),
                post.getAuthor().getEmail(),
                post.getAuthor().getNickname(),
                post.getCreatedAt()
        );
    }

}
