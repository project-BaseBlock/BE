package com.example.baseblock.payment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IamportWebhookPayload {
    private String impUid;
    private String merchantUid; // (옵션)
    private String status;      // (옵션)
}
