package com.example.baseblock.user.controller;

import com.example.baseblock.user.dto.UserProfileUpdateRequest;
import com.example.baseblock.user.dto.UserProfileUpdateRequest;
import com.example.baseblock.user.dto.WalletUpsertRequest;
import com.example.baseblock.user.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserAccountController {

    private final UserAccountService userAccountService;

    // PATCH /user/nickname
    @PatchMapping("/nickname")
    public ResponseEntity<?> updateNickname(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UserProfileUpdateRequest req
    ) {
        String email = principal.getUsername();
        userAccountService.updateNickname(email, req);
        return ResponseEntity.noContent().build(); // 204
    }

    // PATCH /user/wallet
    @PatchMapping("/wallet")
    public ResponseEntity<?> upsertWallet(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody WalletUpsertRequest req
    ) {
        String email = principal.getUsername();
        userAccountService.upsertWallet(email, req);
        return ResponseEntity.noContent().build(); // 204
    }
}
