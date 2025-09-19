package com.example.baseblock.payment.service;

import com.example.baseblock.payment.dto.PaymentReadyResponse;
import com.example.baseblock.payment.dto.PaymentReadyV2Request;
import com.example.baseblock.payment.dto.PaymentResultResponse;
import com.example.baseblock.payment.dto.PaymentVerifyRequest;
import com.example.baseblock.payment.dto.RefundRequest;
import com.example.baseblock.payment.entity.Payment;
import com.example.baseblock.common.PaymentStatus;
import com.example.baseblock.common.ReservationStatus;
import com.example.baseblock.payment.repository.PaymentRepository;
import com.example.baseblock.reservation.entity.Reservation;
import com.example.baseblock.reservation.repository.ReservationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import jakarta.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int LOCK_MINUTES = 10;

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    @Value("${iamport.api-key:}")
    private String iamportApiKey;

    @Value("${iamport.api-secret:}")
    private String iamportApiSecret;

    @Value("${iamport.mock:true}")
    private boolean iamportMock;

    // ========== /payments/ready ==========

    @Transactional
    public PaymentReadyResponse readyPayment(Long reservationId) {
        // 1) 예약 비관적 락
        Reservation r = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("예약이 존재하지 않습니다: " + reservationId));

        // 2) 이미 결제완료 차단
        if (r.getStatus() == ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("이미 결제 완료된 예약입니다.");
        }

        // 3) RESERVED 상태에서만 결제 준비 허용
        if (r.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("결제 준비 불가 상태입니다. 새로 좌석을 선택해 예약을 생성하세요.");
        }

        // 4) 락 만료 확인
        LocalDateTime now = LocalDateTime.now(KST);
        if (r.getLockExpiresAt() != null && r.getLockExpiresAt().isBefore(now)) {
            r.setStatus(ReservationStatus.EXPIRED);
            r.setLockExpiresAt(null);
            throw new IllegalStateException("좌석 홀드가 만료되었습니다. 예약을 다시 생성하세요.");
        }

        // 5) 기존 READY 재사용(락 유효 + READY 존재)
        if (r.getLockExpiresAt() != null && r.getLockExpiresAt().isAfter(now)) {
            Optional<Payment> ready = paymentRepository
                    .findTopByReservation_IdAndStatusOrderByIdDesc(reservationId, PaymentStatus.READY);
            if (ready.isPresent()) {
                Payment p = ready.get();
                return PaymentReadyResponse.builder()
                        .reservationId(reservationId)
                        .merchantUid(p.getMerchantUid())
                        .amount(p.getAmount())
                        .build();
            } else {
                throw new IllegalStateException("이미 결제 진행 중입니다. 잠시 후 다시 시도하세요.");
            }
        }

        // 6) 서버 기준 금액 산출 (Reservation.getTotalAmount() 사용)
        int amount = r.getTotalAmount();

        // 7) 기존 READY가 있으면 멱등 재사용(금액 일치 확인)
        Optional<Payment> existingReady = paymentRepository
                .findTopByReservation_IdAndStatusOrderByIdDesc(reservationId, PaymentStatus.READY);
        if (existingReady.isPresent()) {
            Payment p = existingReady.get();
            if (p.getAmount() != amount) {
                throw new IllegalStateException("기존 결제 준비 금액과 불일치합니다.");
            }
            applyLock(r);
            return PaymentReadyResponse.builder()
                    .reservationId(reservationId)
                    .merchantUid(p.getMerchantUid())
                    .amount(p.getAmount())
                    .build();
        }

        // 8) 신규 READY 생성
        String merchantUid = buildMerchantUid(reservationId);
        Payment p = Payment.builder()
                .reservation(r)
                .merchantUid(merchantUid)
                .amount(amount)
                .status(PaymentStatus.READY)
                .build();
        paymentRepository.save(p);

        // 9) 좌석 락 갱신
        applyLock(r);

        return PaymentReadyResponse.builder()
                .reservationId(reservationId)
                .merchantUid(merchantUid)
                .amount(amount)
                .build();
    }

    @Transactional
    public PaymentReadyResponse readyPaymentV2(PaymentReadyV2Request req) {
        // ✅ V2에서는 stadiumId/zoneName/seatNumbers 등 "메타"를 더 이상 사용하지 않는다.
        //    전달되더라도 무시하고, reservationId만으로 금액 산출/락/멱등 처리.
        Long rid = req.getReservationId();
        if (rid == null) {
            throw new IllegalArgumentException("reservationId는 필수입니다.");
        }
        log.info("[PAY] readyPaymentV2 delegating to readyPayment. rid={}", rid);
        return readyPayment(rid);
    }

    private void applyLock(Reservation r) {
        r.setStatus(ReservationStatus.RESERVED);
        r.setLockExpiresAt(LocalDateTime.now(KST).plusMinutes(LOCK_MINUTES));
    }

    private String buildMerchantUid(Long reservationId) {
        String ts = LocalDateTime.now(KST).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "BB-" + reservationId + "-" + ts;
    }

    // ========== /payments/verify ==========

    @Transactional
    public PaymentResultResponse verifyAndComplete(PaymentVerifyRequest req) {
        if (req.getMerchantUid() == null || req.getMerchantUid().isBlank()) {
            return PaymentResultResponse.fail("merchantUid가 없습니다.");
        }

        Payment p = paymentRepository.findByMerchantUid(req.getMerchantUid())
                .orElseThrow(() -> new EntityNotFoundException("merchantUid가 유효하지 않습니다."));

        if (p.getStatus() == PaymentStatus.PAID) {
            return PaymentResultResponse.ok(p.getImpUid(), p.getMerchantUid(), p.getPaidAt());
        }

        Reservation r = p.getReservation();

        // MOCK 모드: 즉시 처리
        if (iamportMock) {
            if (req.getImpUid() != null && req.getImpUid().startsWith("fail_")) {
                p.markFailed();
                return PaymentResultResponse.fail("mock failure");
            }
            p.markPaid(req.getImpUid() != null ? req.getImpUid() : "imp_LOCAL_TEST");
            r.setStatus(ReservationStatus.CONFIRMED);
            r.setLockExpiresAt(null);
            return PaymentResultResponse.ok(p.getImpUid(), p.getMerchantUid(), p.getPaidAt());
        }

        // 실모드: 입력/설정 방어
        if (req.getImpUid() == null || req.getImpUid().isBlank()) {
            return PaymentResultResponse.fail("impUid가 없습니다.");
        }
        if (iamportApiKey == null || iamportApiKey.isBlank() ||
                iamportApiSecret == null || iamportApiSecret.isBlank()) {
            return PaymentResultResponse.fail("Iamport API Key/Secret이 설정되지 않았습니다.");
        }

        String accessToken = getIamportAccessToken();
        IamportPayment iamport = getIamportPayment(req.getImpUid(), accessToken);

        boolean amountOk   = (iamport.amount == p.getAmount());
        boolean merchantOk = p.getMerchantUid().equals(iamport.merchantUid);
        boolean statusOk   = "paid".equalsIgnoreCase(iamport.status);

        if (amountOk && merchantOk && statusOk) {
            p.markPaid(req.getImpUid());
            r.setStatus(ReservationStatus.CONFIRMED);
            r.setLockExpiresAt(null);
            return PaymentResultResponse.ok(p.getImpUid(), p.getMerchantUid(), p.getPaidAt());
        } else {
            p.markFailed();
            return PaymentResultResponse.fail("검증 실패: amountOk=" + amountOk + ", merchantOk=" + merchantOk + ", statusOk=" + statusOk);
        }
    }

    // ===== Iamport =====

    private String getIamportAccessToken() {
        String url = "https://api.iamport.kr/users/getToken";
        RestTemplate rt = new RestTemplate();
        ObjectMapper om = new ObjectMapper();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        Map<String, String> body = Map.of(
                "imp_key", iamportApiKey,
                "imp_secret", iamportApiSecret
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = rt.postForEntity(url, entity, String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Iamport 토큰 발급 실패: " + resp.getStatusCode());
        }
        try {
            JsonNode root = om.readTree(resp.getBody());
            return root.path("response").path("access_token").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Iamport 토큰 파싱 실패", e);
        }
    }

    private IamportPayment getIamportPayment(String impUid, String accessToken) {
        String url = "https://api.iamport.kr/payments/" + impUid;
        RestTemplate rt = new RestTemplate();
        ObjectMapper om = new ObjectMapper();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> resp = rt.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Iamport 결제 조회 실패: " + resp.getStatusCode());
        }
        try {
            JsonNode root = om.readTree(resp.getBody()).path("response");
            IamportPayment p = new IamportPayment();
            p.status = root.path("status").asText();
            p.merchantUid = root.path("merchant_uid").asText();
            p.amount = root.path("amount").asInt();
            return p;
        } catch (Exception e) {
            throw new IllegalStateException("Iamport 결제 파싱 실패", e);
        }
    }

    private static class IamportPayment {
        String status;
        String merchantUid;
        int amount;
    }

    // ========== Webhook (실모드 시 멱등 정합) ==========

    @Transactional
    public void handleWebhook(String impUid) {
        if (iamportMock) return;

        String token = getIamportAccessToken();
        IamportPayment iamport = getIamportPayment(impUid, token);

        Payment p = paymentRepository.findByMerchantUid(iamport.merchantUid).orElse(null);
        if (p == null) return;

        if (p.getStatus() == PaymentStatus.PAID || p.getStatus() == PaymentStatus.REFUNDED) return;

        boolean amountOk   = (iamport.amount == p.getAmount());
        boolean merchantOk = p.getMerchantUid().equals(iamport.merchantUid);
        boolean statusOk   = "paid".equalsIgnoreCase(iamport.status);

        Reservation r = p.getReservation();
        if (amountOk && merchantOk && statusOk) {
            p.markPaid(impUid);
            r.setStatus(ReservationStatus.CONFIRMED);
            r.setLockExpiresAt(null);
        } else {
            p.markFailed();
        }
    }

    // ========== Refund ==========

    @Transactional
    public PaymentResultResponse refundByMerchantUid(RefundRequest req) {
        Payment p = paymentRepository.findByMerchantUid(req.getMerchantUid())
                .orElseThrow(() -> new EntityNotFoundException("merchantUid가 유효하지 않습니다."));
        if (p.getStatus() != PaymentStatus.PAID) {
            return PaymentResultResponse.fail("결제 완료 상태가 아니어서 환불할 수 없습니다.");
        }

        String token = getIamportAccessToken();
        String url = "https://api.iamport.kr/payments/cancel";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String,Object> body = Map.of(
                "merchant_uid", p.getMerchantUid(),
                "reason", java.util.Optional.ofNullable(req.getReason()).orElse("user_cancel")
        );
        ResponseEntity<String> resp =
                new RestTemplate().postForEntity(url, new HttpEntity<>(body, headers), String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            return PaymentResultResponse.fail("아임포트 환불 실패: " + resp.getStatusCode());
        }

        p.setStatus(PaymentStatus.REFUNDED);
        Reservation r = p.getReservation();
        r.setStatus(ReservationStatus.CANCELED);
        r.setLockExpiresAt(null);
        return PaymentResultResponse.ok(p.getImpUid(), p.getMerchantUid(), LocalDateTime.now());
    }

}
