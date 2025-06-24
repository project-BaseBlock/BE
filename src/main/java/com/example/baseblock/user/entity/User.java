package com.example.baseblock.user.entity;

import com.example.baseblock.board.entity.Comment;
import com.example.baseblock.board.entity.Post;
import com.example.baseblock.common.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private LocalDateTime userCreatedAt;

    @Column
    private LocalDateTime userUpdatedAt;

    @Column
    private LocalDateTime userDeletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;    // user나 admin

    @OneToMany(mappedBy = "author")
    private List<Post> posts;

    @OneToMany(mappedBy = "author")
    private List<Comment> comments;

    @Builder
    public User(String email, String password, String nickname, LocalDateTime userCreatedAt) {
        this.email = email;
        this.password  = password;
        this.nickname = nickname;
        this.userCreatedAt = userCreatedAt;
        this.role = Role.USER;;
    }

    @PreUpdate
    public void onUpdate() {
        this.userUpdatedAt = LocalDateTime.now();
    }
    public void changeRole(Role newRole) {
        this.role = newRole;
    }

}
