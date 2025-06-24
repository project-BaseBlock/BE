package com.example.baseblock.admin.dto;

import com.example.baseblock.common.Role;
import com.example.baseblock.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MasterUserResponse {

    private Long id;
    private String email;
    private String nickname;
    private Role role;

    public static MasterUserResponse fromEntity(User user) {
        return MasterUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }

}
