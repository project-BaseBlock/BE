package com.example.baseblock.user.controller;

import com.example.baseblock.user.dto.AddUserRequest;
import com.example.baseblock.user.dto.LoginRequest;
import com.example.baseblock.user.dto.LoginResponse;
import com.example.baseblock.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserApiController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AddUserRequest request) {
        userService.save(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 로그인
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    // 로그아웃 (JWT 방식은 서버에서 할 게 없음)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // 토큰을 클라이언트가 삭제하도록 안내만 함
        return ResponseEntity.ok("로그아웃 되었습니다. 클라이언트는 토큰을 삭제하세요.");
    }

}