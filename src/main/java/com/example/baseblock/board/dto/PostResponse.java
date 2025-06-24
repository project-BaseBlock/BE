package com.example.baseblock.board.dto;

import com.example.baseblock.board.entity.Post;
import lombok.Getter;

@Getter
public class PostResponse {

    private Long id;
    private  String title;
    private String content;
    private String author;

    public static PostResponse fromEntity(Post post) {
        PostResponse response = new PostResponse();
        response.id = post.getId();
        response.title = post.getTitle();
        response.content = post.getContent();
        response.author = post.getAuthor().getNickname();
        return response;
    }

}
