package com.example.baseblock.ticket.service;

import com.example.baseblock.blockchain.NftService; // ✅ 클레임(온체인 이관)에 필요
import com.example.baseblock.payment.entity.Payment;
import com.example.baseblock.ticket.dto.TicketResponse;
import com.example.baseblock.ticket.entity.Ticket;
import com.example.baseblock.ticket.repository.TicketRepository;
import com.example.baseblock.user.entity.User;
import com.example.baseblock.user.repository.UserRepository;
import com.example.baseblock.reservation.entity.Reservation;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final NftService nftService;

    /** 현재 로그인 사용자 조회 (email 기반 가정) */
    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다: " + email));
    }

    /** 내 티켓 목록 (페이지네이션) */
    @Transactional(readOnly = true)
    public Page<TicketResponse> getMyTickets(Pageable pageable) {
        return ticketRepository.findByUserOrderByIssuedAtDesc(currentUser(), pageable)
                .map(TicketResponse::fromEntity);
    }

    /** 내 티켓 단건 조회 (ticketId) */
    @Transactional(readOnly = true)
    public TicketResponse getMyTicketById(Long ticketId) {
        Ticket t = ticketRepository.findByIdAndUser(ticketId, currentUser())
                .orElseThrow(() -> new EntityNotFoundException("티켓이 없거나 접근 권한이 없습니다."));
        return TicketResponse.fromEntity(t);
    }

    /** 결제 직후 예약ID로 생성된 내 티켓 조회 (최신 1건) */
    @Transactional(readOnly = true)
    public TicketResponse getMyTicketByReservationId(Long reservationId) {
        User me = currentUser();
        Ticket t = ticketRepository.findFirstByReservation_IdAndUser_IdOrderByIdDesc(reservationId, me.getId())
                .orElseThrow(() -> new EntityNotFoundException("해당 예약으로 생성된 티켓이 없거나 권한이 없습니다."));
        return TicketResponse.fromEntity(t);
    }

    /** 결제 성공 이후 민팅 결과를 기록 (멱등) — 기존 동작 유지 */
    @Transactional
    public Ticket recordMintIfAbsent(Reservation r, Payment p, Long tokenId, String txHash) {
        Long gameId    = r.getGameSchedule().getGameId(); // GameSchedule PK는 'gameId'
        Long seatNumId = r.getSeatNum().getId();

        return ticketRepository.findByGameSchedule_IdAndSeatNum_Id(gameId, seatNumId)
                .orElseGet(() -> {
                    try {
                        return ticketRepository.save(
                                Ticket.builder()
                                        .user(r.getUser())
                                        .reservation(r)
                                        .payment(p)
                                        .gameSchedule(r.getGameSchedule())
                                        .seatNum(r.getSeatNum())
                                        .tokenId(tokenId)
                                        .txHash(txHash)
                                        .status("VALID")
                                        .issuedAt(LocalDateTime.now())
                                        // chainStatus/onchainOwner는 기존 로직 호환을 위해 여기서는 건드리지 않음
                                        .build()
                        );
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        // 유니크 제약 경합 시 재조회로 멱등 보장
                        return ticketRepository.findByGameSchedule_IdAndSeatNum_Id(gameId, seatNumId)
                                .orElseThrow(() -> new IllegalStateException("티켓 저장 충돌 후 재조회 실패"));
                    }
                });
    }

    // =======================
    // [추가]보관지갑 → 사용자 지갑 클레임(이관)
    // =======================
    @Transactional
    public TicketResponse claimMyTicket(Long ticketId) {
        User me = currentUser();
        // 동시성 안전: 행 잠금 조회 (레포지토리에 findByIdForUpdate 추가되어 있어야 함)
        Ticket t = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("티켓이 존재하지 않습니다."));

        // 소유자 확인
        if (!t.getUser().getId().equals(me.getId())) {
            throw new IllegalStateException("본인 소유 티켓이 아닙니다.");
        }

        // 이미 사용자 지갑으로 소유 중이면 멱등
        if ("MINTED".equalsIgnoreCase(t.getChainStatus())) {
            return TicketResponse.fromEntity(t);
        }

        // 사용자 지갑 확인
        String userWallet = me.getWalletAddress();
        if (userWallet == null || userWallet.isBlank()) {
            throw new IllegalStateException("지갑 주소가 없습니다. 마이페이지에서 지갑 주소를 등록하세요.");
        }

        // tokenId가 있어야 이관 가능(커스터디얼 민팅된 상태)
        if (t.getTokenId() == null) {
            throw new IllegalStateException("이관할 tokenId가 없습니다. 민팅 상태를 확인하세요.");
        }

        // 온체인 이관(custody → user)
        try {
            String tx = nftService.safeTransferFromCustody(userWallet, BigInteger.valueOf(t.getTokenId()));
            t.setChainStatus("MINTED");
            t.setOnchainOwner(userWallet);
            t.setTxHash(tx);
            ticketRepository.save(t);
        } catch (Exception e) {
            throw new IllegalStateException("NFT 이관 실패: " + e.getMessage(), e);
        }

        return TicketResponse.fromEntity(t);
    }
}
