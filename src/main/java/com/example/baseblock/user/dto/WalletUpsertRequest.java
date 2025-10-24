package com.example.baseblock.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@ToString
public class WalletUpsertRequest {

    @NotBlank(message = "지갑 주소는 비어 있을 수 없습니다.")
    @Pattern(
            regexp = "^0x[a-fA-F0-9]{40}$",
            message = "지갑 주소 형식이 올바르지 않습니다. (예: 0x로 시작하는 40자리 16진수)"
    )
    private String walletAddress;
}
