package com.example.baseblock.payment.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentReadyResponse {
    private Long reservationId;
    private String merchantUid;
    private int amount;
    // (선택) 프론트 표시용
    private String buyerName;
    private String buyerEmail;
    private String buyerTel;
}