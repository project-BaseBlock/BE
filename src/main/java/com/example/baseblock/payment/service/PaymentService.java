package com.example.baseblock.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.baseblock.payment.dto.PaymentReadyResponse;
import com.example.baseblock.payment.dto.PaymentResultResponse;
import com.example.baseblock.payment.dto.PaymentVerifyRequest;
import com.example.baseblock.payment.entity.Payment;
import com.example.baseblock.common.PaymentStatus;
import com.example.baseblock.payment.repository.PaymentRepository;
import com.example.baseblock.reservation.entity.Reservation;
import com.example.baseblock.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    @Value("${iamport.api-key}")
    private String iamportApiKey;

    @Value("${iamport.api-secret}")
    private String iamportApiSecret;

    /**
     * 1) 결제 준비:
     *  - 서버에서 금액 산출
     *  - merchantUid 서버 생성
     *  - Payment READY 저장
     */
    @Transactional
    public PaymentReadyResponse readyPayment(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("reservation not found: " + reservationId));

        int amount = resolveAmount(r); // 서버 기준 금액
        String merchantUid = buildMerchantUid(r.getId());

        Payment p = Payment.builder()
                .reservation(r)
                .merchantUid(merchantUid)
                .amount(amount)
                .status(PaymentStatus.READY)
                .build();

        paymentRepository.save(p);

        return PaymentReadyResponse.builder()
                .merchantUid(merchantUid)
                .amount(amount)
                .build();
    }

    /**
     * 2) 결제 검증:
     *  - impUid로 아임포트 서버 조회
     *  - 금액/merchantUid/상태 일치 확인
     *  - 일치 시 payment.markPaid(impUid), reservation 상태도 PAID로 갱신
     *  - 불일치 시 payment.markFailed()
     */
    @Transactional
    public PaymentResultResponse verifyAndComplete(PaymentVerifyRequest req) {
        Payment p = paymentRepository.findByMerchantUid(req.getMerchantUid())
                .orElseThrow(() -> new IllegalArgumentException("payment not found: " + req.getMerchantUid()));

        Reservation r = reservationRepository.findById(req.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("reservation not found: " + req.getReservationId()));

        if (!p.getReservation().getId().equals(r.getId())) {
            return PaymentResultResponse.fail("merchantUid와 reservationId 매칭 불일치");
        }

        // Iamport 서버 검증
        String accessToken = getIamportAccessToken();
        IamportPayment iamport = getIamportPayment(req.getImpUid(), accessToken);

        boolean amountOk   = (iamport.amount == p.getAmount());
        boolean merchantOk = p.getMerchantUid().equals(iamport.merchantUid);
        boolean statusOk   = "paid".equalsIgnoreCase(iamport.status);

        if (amountOk && merchantOk && statusOk) {
            p.markPaid(req.getImpUid());              // ← 엔티티 도메인 메서드 활용

            // Reservation도 PAID로 전이(프로젝트 구현에 맞춰 두 가지 경로 시도)
            markReservationPaid(r);

            return PaymentResultResponse.ok(p.getImpUid(), p.getMerchantUid(), p.getPaidAt());
        } else {
            p.markFailed();                           // ← 엔티티 도메인 메서드 활용
            String msg = "검증 실패: amountOk=" + amountOk + ", merchantOk=" + merchantOk + ", statusOk=" + statusOk;
            return PaymentResultResponse.fail(msg);
        }
    }

    // ===== 내부 유틸 =====

    /** 서버 기준 금액 산출: Reservation.totalAmount()가 있으면 사용, 없으면 좌석/존 기반 재계산 로직을 붙이세요. */
    private int resolveAmount(Reservation r) {
        try {
            var m = Reservation.class.getMethod("getTotalAmount");
            Object val = m.invoke(r);
            if (val instanceof Number && ((Number) val).intValue() > 0) {
                return ((Number) val).intValue();
            }
        } catch (Exception ignore) { /* fall through */ }

        // TODO: SeatZone/SeatNum/Reservation-Seat 매핑을 통해 금액을 재계산하는 로직을 연결하세요.
        //  ex) zone 가격 × 좌석수 합계 (레드/오렌지/블루/네이비/그린)
        throw new IllegalStateException("서버 금액 산출 로직이 필요합니다(Reservation.totalAmount 또는 좌석기반 계산).");
    }

    /** 주문번호 생성 규칙(예시): baseblock_{reservationId}_{epochMillis} */
    private String buildMerchantUid(Long reservationId) {
        return "baseblock_" + reservationId + "_" + System.currentTimeMillis();
    }

    /** Reservation 상태를 PAID로 전이: markPaid() 또는 setStatus("PAID") 중 가용한 메서드 사용 */
    private void markReservationPaid(Reservation r) {
        try {
            var m = Reservation.class.getMethod("markPaid");
            m.invoke(r);
        } catch (Exception e1) {
            try {
                var m2 = Reservation.class.getMethod("setStatus", String.class);
                m2.invoke(r, "PAID");
            } catch (Exception e2) {
                // 프로젝트에 맞춰 명시적 예외로 바꿔도 됩니다.
                throw new IllegalStateException("Reservation 상태 전이 메서드가 필요합니다(markPaid 또는 setStatus).");
            }
        }
    }

    /** 아임포트 AccessToken */
    private String getIamportAccessToken() {
        String url = "https://api.iamport.kr/users/getToken";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        Map<String, String> body = new HashMap<>();
        body.put("imp_key", iamportApiKey);
        body.put("imp_secret", iamportApiSecret);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);
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

    /** 아임포트 결제 단건 조회 */
    private IamportPayment getIamportPayment(String impUid, String accessToken) {
        String url = "https://api.iamport.kr/payments/" + impUid;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Iamport 결제 조회 실패: " + resp.getStatusCode());
        }
        try {
            JsonNode root = om.readTree(resp.getBody()).path("response");
            IamportPayment p = new IamportPayment();
            p.status = root.path("status").asText();             // ex) "paid"
            p.merchantUid = root.path("merchant_uid").asText();  // 우리 주문번호
            p.amount = root.path("amount").asInt();              // 결제 금액
            return p;
        } catch (Exception e) {
            throw new IllegalStateException("Iamport 결제 파싱 실패", e);
        }
    }

    /** 내부 전용 최소 DTO */
    private static class IamportPayment {
        String status;
        String merchantUid;
        int amount;
    }
}
