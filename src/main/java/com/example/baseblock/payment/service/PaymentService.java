package com.example.baseblock.payment.service;

import com.example.baseblock.blockchain.NftService;
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
import com.example.baseblock.stadium.entity.SeatZone;
import com.example.baseblock.stadium.entity.Stadium;
import com.example.baseblock.ticket.entity.Ticket;
import com.example.baseblock.ticket.repository.TicketRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    private final com.example.baseblock.stadium.repository.SeatNumRepository seatNumRepository;
    private final com.example.baseblock.stadium.repository.SeatZoneRepository seatZoneRepository;
    private final com.example.baseblock.stadium.repository.StadiumRepository stadiumRepository;

    private final NftService nftService;
    private final TicketRepository ticketRepository;

    @Value("${iamport.api-key:}")
    private String iamportApiKey;

    @Value("${iamport.api-secret:}")
    private String iamportApiSecret;

    @Value("${iamport.mock:true}")
    private boolean iamportMock;

    // ===== [ADDED] 온체인 스위치 =====
    @Value("${nft.mint.enabled:true}")
    private boolean mintEnabled;

    @Value("${nft.custodial.enabled:true}")
    private boolean custodialEnabled;

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
                log.info("[PAY/READY reuse] rid={}, merchantUid={}, amount={}",
                        reservationId, p.getMerchantUid(), p.getAmount());
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
            log.info("[PAY/READY reuse2] rid={}, merchantUid={}, amount={}",
                    reservationId, p.getMerchantUid(), p.getAmount());
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
                .status(PaymentStatus.READY) // ← 프로젝트 enum 표기에 맞추세요 (예: READY/ready)
                .build();
        paymentRepository.save(p);

        // 9) 좌석 락 갱신
        applyLock(r);

        log.info("[PAY/READY new] rid={}, merchantUid={}, amount={}",
                reservationId, merchantUid, amount);

        return PaymentReadyResponse.builder()
                .reservationId(reservationId)
                .merchantUid(merchantUid)
                .amount(amount)
                .build();
    }

    @Transactional
    public PaymentReadyResponse readyPaymentV2(PaymentReadyV2Request req) {
        // 0) 필수값
        final Long rid = req.getReservationId();
        if (rid == null) throw new IllegalArgumentException("reservationId는 필수입니다.");

        // 1) 예약 잠금 + 상태 게이트
        Reservation r = reservationRepository.findByIdForUpdate(rid)
                .orElseThrow(() -> new EntityNotFoundException("예약이 존재하지 않습니다: " + rid));
        if (r.getStatus() == ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("이미 결제 완료된 예약입니다.");
        }
        if (r.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("결제 준비 불가 상태입니다. 새로 좌석을 선택해 예약을 생성하세요.");
        }
        LocalDateTime now = LocalDateTime.now(KST);
        if (r.getLockExpiresAt() != null && r.getLockExpiresAt().isBefore(now)) {
            r.setStatus(ReservationStatus.EXPIRED);
            r.setLockExpiresAt(null);
            throw new IllegalStateException("좌석 홀드가 만료되었습니다. 예약을 다시 생성하세요.");
        }

        // 2) 좌석 리스트 정규화
        java.util.List<String> seats = new java.util.ArrayList<>();
        if (req.getSeatNumbers() != null) {
            for (String s : req.getSeatNumbers()) {
                if (s == null) continue;
                String t = s.trim();
                if (!t.isEmpty()) seats.add(t);
            }
            if (seats.size() == 1 && seats.get(0).contains(",")) {
                seats = java.util.Arrays.stream(seats.get(0).split(","))
                        .map(String::trim).filter(str -> !str.isEmpty())
                        .collect(java.util.stream.Collectors.toList());
            }
        }
        if (seats.isEmpty()) throw new IllegalArgumentException("seatNumbers가 비어 있습니다.");
        final int seatCount = seats.size();

        // 3) 구역 단가 조회
        if (req.getStadiumId() == null || req.getZoneName() == null || req.getZoneName().isBlank()) {
            throw new IllegalArgumentException("stadiumId/zoneName이 누락되었습니다.");
        }
        Stadium stadium = stadiumRepository.findById(req.getStadiumId())
                .orElseThrow(() -> new EntityNotFoundException("스타디움 정보를 찾을 수 없습니다."));
        SeatZone zone = seatZoneRepository.findByStadiumAndZoneName(stadium, req.getZoneName())
                .orElseThrow(() -> new EntityNotFoundException("구역 정보를 찾을 수 없습니다."));

        // 4) 총액 = 구역가 × 자리 수
        int amount = zone.getPrice() * seatCount;

        // 로그: 요청 파라미터 요약
        log.info("[PAY/READY v2] rid={}, stadiumId={}, zone={}, seats={}, calcAmount={}",
                rid, req.getStadiumId(), req.getZoneName(), seatCount, amount);

        // 5) 멱등: 기존 READY 재사용(금액 불일치 시 오류)
        Optional<Payment> existing = paymentRepository
                .findTopByReservation_IdAndStatusOrderByIdDesc(rid, PaymentStatus.READY);
        if (existing.isPresent()) {
            Payment p = existing.get();
            if (p.getAmount() != amount) {
                throw new IllegalStateException("기존 결제 준비 금액과 불일치합니다.");
            }
            applyLock(r);
            log.info("[PAY/READY v2 reuse] rid={}, merchantUid={}, amount={}",
                    rid, p.getMerchantUid(), p.getAmount());
            return PaymentReadyResponse.builder()
                    .reservationId(rid)
                    .merchantUid(p.getMerchantUid())
                    .amount(p.getAmount())
                    .build();
        }

        // 6) 새 READY 생성
        String merchantUid = buildMerchantUid(rid);
        Payment p = Payment.builder()
                .reservation(r)
                .merchantUid(merchantUid)
                .amount(amount)
                .status(PaymentStatus.READY)
                .build();
        paymentRepository.save(p);

        // 7) 좌석 락 갱신
        applyLock(r);

        log.info("[PAY/READY v2 new] rid={}, merchantUid={}, amount={}",
                rid, merchantUid, amount);

        // 8) 응답
        return PaymentReadyResponse.builder()
                .reservationId(rid)
                .merchantUid(merchantUid)
                .amount(amount)
                .build();
    }

    private void applyLock(Reservation r) {
        r.setStatus(ReservationStatus.RESERVED);
        r.setLockExpiresAt(LocalDateTime.now(KST).plusMinutes(LOCK_MINUTES));
    }

    private String buildMerchantUid(Long reservationId) {
        String ts = LocalDateTime.now(KST).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String tail = Integer.toString((int)(Math.random() * 900) + 100);
        return "BB-" + reservationId + "-" + ts + "-" + tail;
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
        boolean isMock = iamportMock || req.isMock(); // ← 원시형 boolean이므로 isMock() 사용
        if (isMock) {
            if (req.getImpUid() != null && req.getImpUid().startsWith("fail_")) {
                p.markFailed();
                log.info("[PAY/VERIFY mock] merchantUid={}, result=FAIL(mock)", p.getMerchantUid());
                return PaymentResultResponse.fail("mock failure");
            }
            p.markPaid(req.getImpUid() != null ? req.getImpUid() : "imp_LOCAL_TEST");
            r.setStatus(ReservationStatus.CONFIRMED);
            r.setLockExpiresAt(null);

            // 결제 성공 직후 NFT 민팅 (MOCK)
            try {
                mintTicketsForReservation(p, r); // 내부에서 커스터디얼 분기 처리
            } catch (Exception e) {
                log.error("[NFT] mock 민팅 실패 reservationId={}, err={}", r.getId(), e.getMessage(), e);
            }

            log.info("[PAY/VERIFY mock] merchantUid={}, result=PAID", p.getMerchantUid());
            return PaymentResultResponse.ok(p.getImpUid(), p.getMerchantUid(), p.getPaidAt());
        }


        // 실모드
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

        log.info("[PAY/VERIFY] merchantUid={}, amountOk={}, merchantOk={}, statusOk={}",
                p.getMerchantUid(), amountOk, merchantOk, statusOk);

        if (amountOk && merchantOk && statusOk) {
            p.markPaid(req.getImpUid());
            r.setStatus(ReservationStatus.CONFIRMED);
            r.setLockExpiresAt(null);

            // 결제 성공 직후 NFT 민팅 (실모드)
            try {
                mintTicketsForReservation(p, r); // ← [CHANGED]
            } catch (Exception e) {
                log.error("[NFT] 실모드 민팅 실패 reservationId={}, err={}", r.getId(), e.getMessage(), e);
            }

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

            // 웹훅 경로에서도 민팅 보장
            try {
                mintTicketsForReservation(p, r); // ← [CHANGED]
            } catch (Exception e) {
                log.error("[NFT] webhook 민팅 실패 reservationId={}, err={}", r.getId(), e.getMessage(), e);
            }

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

    // ===== [CHANGED] : 민팅 공통 로직 (결제 성공 시 호출) =====
    private void mintTicketsForReservation(Payment p, Reservation r) throws Exception {
        // 멱등: 같은 경기/좌석이면 스킵
        Long gameId    = r.getGameSchedule().getId();         // ← 엔티티 PK 기준
        Long seatNumId = r.getSeatNum().getId();
        String seatNo  = r.getSeatNum().getNumber();

        if (ticketRepository.findByGameSchedule_IdAndSeatNum_Id(gameId, seatNumId).isPresent()) {
            log.info("[NFT] 이미 티켓 존재 (gameId={}, seatId={}) → 스킵", gameId, seatNumId);
            return;
        }

        // 온체인 스위치 OFF → 오프체인 발급만
        if (!mintEnabled) {
            Ticket t = Ticket.builder()
                    .reservation(r)
                    .payment(p)
                    .user(r.getUser())
                    .gameSchedule(r.getGameSchedule())
                    .seatNum(r.getSeatNum())
                    .status("VALID")
                    .issuedAt(LocalDateTime.now())
                    .chainStatus("ISSUED")          // ← [ADDED]
                    .onchainOwner(null)             // ← [ADDED]
                    .build();
            ticketRepository.save(t);
            log.info("[NFT] mint disabled → 오프체인 발급(ISSUED)");
            return;
        }

        // 대상 주소 결정: 사용자 지갑 또는 보관 지갑
        String userWallet = Optional.ofNullable(r.getUser()).map(u -> u.getWalletAddress()).orElse(null);
        String toAddress  = null;
        String chainStatus = "ISSUED";
        if (userWallet != null && !userWallet.isBlank()) {
            toAddress = userWallet;
            chainStatus = "MINTED";
        } else if (custodialEnabled) {
            toAddress = nftService.getCustodyAddress();
            chainStatus = "CUSTODIAL";
        } else {
            // 커스터디얼 OFF면 온체인 스킵
            Ticket t = Ticket.builder()
                    .reservation(r)
                    .payment(p)
                    .user(r.getUser())
                    .gameSchedule(r.getGameSchedule())
                    .seatNum(r.getSeatNum())
                    .status("VALID")
                    .issuedAt(LocalDateTime.now())
                    .chainStatus("ISSUED")
                    .onchainOwner(null)
                    .build();
            ticketRepository.save(t);
            log.warn("[NFT] user wallet missing & custodial disabled → 온체인 스킵(ISSUED)");
            return;
        }

        // 온체인 민팅 (컨트랙트 시그니처: mintTicket(to, gameId, seatNo) 가정)
        NftService.MintResult res = nftService.mintTicket(
                toAddress,
                java.math.BigInteger.valueOf(r.getGameSchedule().getId()),  // 또는 getGameId() 프로젝트에 맞춤
                r.getSeatNum().getNumber()
        );

        Long tokenId = (res.tokenId() == null) ? null : res.tokenId().longValue();
        String txHash = res.txHash();

        // Ticket 저장
        Ticket t = Ticket.builder()
                .reservation(r)
                .payment(p)
                .user(r.getUser())
                .gameSchedule(r.getGameSchedule())
                .seatNum(r.getSeatNum())
                .tokenId(res.tokenId() == null ? null : res.tokenId().longValue())
                .txHash(res.txHash())
                .status("VALID")
                .issuedAt(LocalDateTime.now())
                .chainStatus(chainStatus)           // ← [ADDED]
                .onchainOwner(toAddress)            // ← [ADDED]
                .build();
        ticketRepository.save(t);

        log.info("[NFT] minted. chainStatus={}, owner={}, tokenId={}, tx={}",
                chainStatus, toAddress, res.tokenId(), res.txHash());
    }
}
