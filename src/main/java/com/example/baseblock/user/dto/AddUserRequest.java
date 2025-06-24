package com.example.baseblock.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AddUserRequest {
    private String email;
    private String password;
    private String nickname;
}
