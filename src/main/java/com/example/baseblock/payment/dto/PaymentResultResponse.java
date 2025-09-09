package com.example.baseblock.payment.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentResultResponse {
    private boolean success;
    private String message;
    private String impUid;
    private String merchantUid;
    private LocalDateTime paidAt;

    public static PaymentResultResponse ok(String impUid, String merchantUid, LocalDateTime paidAt) {
        return PaymentResultResponse.builder()
                .success(true).message("결제 완료")
                .impUid(impUid).merchantUid(merchantUid).paidAt(paidAt)
                .build();
    }

    public static PaymentResultResponse fail(String msg) {
        return PaymentResultResponse.builder()
                .success(false).message(msg).build();
    }
}