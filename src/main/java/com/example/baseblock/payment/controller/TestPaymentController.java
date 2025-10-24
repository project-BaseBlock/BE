package com.example.baseblock.payment.controller;

import com.example.baseblock.payment.dto.PaymentVerifyRequest;
import com.example.baseblock.payment.dto.PaymentReadyResponse;
import com.example.baseblock.payment.service.PaymentService;
import com.example.baseblock.payment.dto.PaymentResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments/_test")
@RequiredArgsConstructor
public class TestPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/ready")
    public PaymentReadyResponse ready(@RequestParam Long reservationId) {
        return paymentService.readyPayment(reservationId);
    }

    @PostMapping("/verify")
    public PaymentResultResponse verify(
            @RequestParam String merchantUid,
            @RequestParam(required = false, defaultValue = "imp_LOCAL_TEST") String impUid
    ) {
        PaymentVerifyRequest req = new PaymentVerifyRequest();
        req.setMerchantUid(merchantUid);
        req.setImpUid(impUid);
        return paymentService.verifyAndComplete(req);
    }
}
