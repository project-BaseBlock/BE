package com.example.baseblock.payment.controller;

import com.example.baseblock.payment.dto.PaymentReadyResponse;
import com.example.baseblock.payment.dto.PaymentResultResponse;
import com.example.baseblock.payment.dto.PaymentVerifyRequest;
import com.example.baseblock.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 준비 (금액 산출 + merchantUid 생성)")
    @PostMapping("/ready/{reservationId}")
    public PaymentReadyResponse ready(@PathVariable Long reservationId) {
        return paymentService.readyPayment(reservationId);
    }

    @Operation(summary = "결제 검증 (impUid/merchantUid/금액 비교)")
    @PostMapping("/verify")
    public PaymentResultResponse verify(@RequestBody PaymentVerifyRequest req) {
        return paymentService.verifyAndComplete(req);
    }
}
