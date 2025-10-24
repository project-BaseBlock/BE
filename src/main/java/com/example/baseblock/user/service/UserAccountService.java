package com.example.baseblock.user.service;

import com.example.baseblock.user.dto.UserProfileUpdateRequest;
import com.example.baseblock.user.dto.WalletUpsertRequest;
import com.example.baseblock.user.entity.User;
import com.example.baseblock.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.example.baseblock.user.dto.UserProfileUpdateRequest;
import com.example.baseblock.user.dto.WalletUpsertRequest;
import com.example.baseblock.user.entity.User;
import com.example.baseblock.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserAccountService {

    private final UserRepository userRepository;

    private User getByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    @Transactional
    public void updateNickname(String email, UserProfileUpdateRequest req) {
        User user = getByEmailOrThrow(email);
        String before = user.getNickname();
        user.setNickname(req.getNickname());
        log.info("[UserAccount] nickname updated: {} -> {} (email={})", before, req.getNickname(), email);
        // JPA dirty checking
    }

    @Transactional
    public void upsertWallet(String email, WalletUpsertRequest req) {
        User user = getByEmailOrThrow(email);
        String before = user.getWalletAddress();
        user.setWalletAddress(req.getWalletAddress());
        log.info("[UserAccount] wallet upserted: {} -> {} (email={})", before, req.getWalletAddress(), email);
        // JPA dirty checking
    }
}
