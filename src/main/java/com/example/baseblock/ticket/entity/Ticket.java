package com.example.baseblock.ticket.entity;

import com.example.baseblock.reservation.entity.Reservation;
import com.example.baseblock.payment.entity.Payment;
import com.example.baseblock.user.entity.User;
import com.example.baseblock.game.entity.GameSchedule;
import com.example.baseblock.stadium.entity.SeatNum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ticket",
        uniqueConstraints = {
                // 같은 경기 같은 좌석은 1장만 발급
                @UniqueConstraint(name = "uk_ticket_game_seat", columnNames = {"game_schedule_id", "seat_num_id"})
        },
        indexes = {
                @Index(name = "ix_ticket_reservation_id", columnList = "reservation_id"),
                @Index(name = "ix_ticket_user_issued_at", columnList = "user_id")
        }
)
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Ticket {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예약(필수)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // 결제(필수)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    // 소유 유저(필수)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 경기(필수)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_schedule_id", nullable = false)
    private GameSchedule gameSchedule;

    // 좌석(필수)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_num_id", nullable = false)
    private SeatNum seatNum;

    // ===== 온체인 정보 =====
    /** NFT 토큰 ID (컨트랙트에서 이벤트로 받은 tokenId; Long 사용) */
    @Column(name = "token_id")
    private Long tokenId;

    /** 마지막 온체인 트랜잭션 해시 */
    @Column(name = "tx_hash", length = 100)
    private String txHash;

    /** 기존 비즈니스 상태(예: VALID/VOID/MINT_PENDING 등) */
    @Column(name = "status", length = 16)
    private String status;

    /** 발급 시각 */
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    // ====== ★ 추가: 커스터디얼/클레임을 위한 체인 상태/소유자 ======
    /**
     * 체인 상태: ISSUED(오프체인만), CUSTODIAL(보관지갑으로 민팅됨), MINTED(사용자지갑으로 이관/민팅됨)
     * 기존 status와 분리해서, 온체인 소유권 흐름만 분명히 관리
     */
    @Column(name = "chain_status", length = 16) // ISSUED | CUSTODIAL | MINTED
    private String chainStatus;

    /** 현재 온체인 소유자 주소(보관지갑 or 사용자지갑) */
    @Column(name = "onchain_owner", length = 100)
    private String onchainOwner;

    @PrePersist
    void onCreate() {
        if (issuedAt == null) {
            issuedAt = LocalDateTime.now();
        }
        // 기본 체인 상태 값
        if (chainStatus == null) {
            chainStatus = "ISSUED";
        }
    }

    // ===== 편의 메서드 =====
    public String seatKey() {
        // SeatNum.number 필드명에 맞춰 수정됨
        return seatNum != null ? seatNum.getNumber() : null;
    }

    public boolean isChainIssued()     { return "ISSUED".equalsIgnoreCase(chainStatus); }
    public boolean isChainCustodial()  { return "CUSTODIAL".equalsIgnoreCase(chainStatus); }
    public boolean isChainMinted()     { return "MINTED".equalsIgnoreCase(chainStatus); }
}
