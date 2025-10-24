package com.example.baseblock.user.entity;

import com.example.baseblock.board.entity.Comment;
import com.example.baseblock.board.entity.Post;
import com.example.baseblock.common.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "user",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_wallet", columnList = "wallet_address")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
                // wallet_address는 @Column(unique=true)로 처리 (null 다중 허용 필요시 보통 이 방식 사용)
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false)
    private LocalDateTime userCreatedAt;

    @Column
    private LocalDateTime userUpdatedAt;

    @Column
    private LocalDateTime userDeletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;    // user나 admin

    @OneToMany(mappedBy = "author")
    private List<Post> posts = new ArrayList<>();;

    @OneToMany(mappedBy = "author")
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public User(String email, String password, String nickname, LocalDateTime userCreatedAt) {
        this.email = email;
        this.password  = password;
        this.nickname = nickname;
        this.userCreatedAt = userCreatedAt;
        this.role = Role.USER;;
    }

    // 생성 시각/기본 롤 자동 세팅
    @PrePersist
    public void onCreate() {
        if (this.userCreatedAt == null) {
            this.userCreatedAt = LocalDateTime.now();
        }
        if (this.role == null) {
            this.role = Role.USER;
        }
    }


    @PreUpdate
    public void onUpdate() {
        this.userUpdatedAt = LocalDateTime.now();
    }

    public void changeRole(Role newRole) {
        this.role = newRole;
    }

    // [ADDED] 메타마스크 등 블록체인용 지갑 주소
    @Column(name = "wallet_address", length = 64, unique = true)
    private String walletAddress; // 예: 0x4094e65129122b728cc87692A6e305b00FfA43C7

    // [ADDED] 도메인 메서드: 닉네임/지갑 수정 (서비스에서 사용)
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    // 필요 시: 간단 검증 메서드
    public boolean hasWallet() {
        return walletAddress != null && walletAddress.startsWith("0x");
    }

}
