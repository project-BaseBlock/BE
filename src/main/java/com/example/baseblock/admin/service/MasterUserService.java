package com.example.baseblock.admin.service;

import com.example.baseblock.common.Role;
import com.example.baseblock.user.dto.UserResponse;
import com.example.baseblock.user.entity.User;
import com.example.baseblock.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MasterUserService {

    private final UserRepository userRepository;

    // 전체 유저 조회
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 권한 변경 (USER <-> ADMIN)
    @Transactional
    public void changeRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
        user.changeRole(newRole);
    }

    // 유저 삭제
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

}
