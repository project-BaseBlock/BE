package com.example.baseblock.payment.controller;

import com.example.baseblock.payment.dto.*;
import com.example.baseblock.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
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

    @PostMapping("/ready/v2")
    public PaymentReadyResponse readyV2(@RequestBody @Valid PaymentReadyV2Request req) {
        //과거 호환 엔드포인트: 넘어온 메타(stadiumId/zoneName/seatNumbers)는 이제 무시
        log.info("[API] POST /payments/ready/v2 rid={}, (stadiumId/zone/seats 무시)",
                req.getReservationId());
        return paymentService.readyPaymentV2(req); // 내부에서 readyPayment로 위임
    }

    @Operation(summary = "결제 검증 (impUid/merchantUid/금액 비교)")
    @PostMapping("/verify")
    public PaymentResultResponse verify(@RequestBody PaymentVerifyRequest req) {
        return paymentService.verifyAndComplete(req); // 실/목 공용
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody IamportWebhookPayload payload) {
        paymentService.handleWebhook(payload.getImpUid());
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/refund")
    public PaymentResultResponse refund(@RequestBody RefundRequest req) {
        return paymentService.refundByMerchantUid(req);
    }

}
