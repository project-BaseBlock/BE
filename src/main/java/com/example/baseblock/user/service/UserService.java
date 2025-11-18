package com.example.baseblock.user.service;

import com.example.baseblock.common.Role;
import com.example.baseblock.config.JwtTokenProvider;
import com.example.baseblock.user.dto.AddUserRequest;
import com.example.baseblock.user.dto.LoginRequest;
import com.example.baseblock.user.dto.LoginResponse;
import com.example.baseblock.user.entity.User;
import com.example.baseblock.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public Long save(AddUserRequest dto) {
        String encodedPassword = passwordEncoder.encode(dto.getPassword()); // 암호화
        return  userRepository.save(User.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .nickname(dto.getNickname())
                .userCreatedAt(LocalDateTime.now())
                .build()).getId();
    }

    // 로그인
    public LoginResponse login (LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name()); //토큰 발급
        return new LoginResponse(token);
    }

}
