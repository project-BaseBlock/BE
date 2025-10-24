package com.example.baseblock.payment.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentVerifyRequest {
    private String impUid;
    private String merchantUid;
    private Long reservationId;

    private boolean mock;
}