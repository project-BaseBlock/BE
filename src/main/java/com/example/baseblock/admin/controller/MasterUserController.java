package com.example.baseblock.admin.controller;

import com.example.baseblock.admin.service.MasterUserService;
import com.example.baseblock.common.Role;
import com.example.baseblock.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MASTER')")
public class MasterUserController {

    private final MasterUserService masterUserService;

    // 1. 전체 유저 조회
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return masterUserService.getAllUsers();
    }

    // 2. USER → ADMIN 권한 변경
    @PatchMapping("/{userId}/grant-admin")
    public void grantAdminRole(@PathVariable Long userId) {
        masterUserService.changeRole(userId, Role.ADMIN);
    }

    // 3. ADMIN → USER 권한 변경
    @PatchMapping("/{userId}/revoke-admin")
    public void revokeAdminRole(@PathVariable Long userId) {
        masterUserService.changeRole(userId, Role.USER);
    }

    // 4. 계정 삭제
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        masterUserService.deleteUser(userId);
    }

}
