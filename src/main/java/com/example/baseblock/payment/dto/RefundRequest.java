package com.example.baseblock.payment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {
    private String merchantUid;
    private String reason; // 옵션
}

