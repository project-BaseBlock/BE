package com.example.baseblock.payment.entity;

import com.example.baseblock.common.PaymentStatus;
import com.example.baseblock.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_merchant_uid", columnNames = "merchant_uid"),
                @UniqueConstraint(name = "uk_payment_imp_uid", columnNames = "imp_uid")
        },
        indexes = {
                @Index(name = "idx_payment_reservation_id", columnList = "reservation_id"),
                @Index(name = "idx_payment_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 예약 1건 : 결제 1건(현재 설계) */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    /** Iamport 결제 고유 ID (결제 완료 후 세팅) */
    @Column(name = "imp_uid", unique = true)
    private String impUid;

    /** 상점 주문번호 - 반드시 "서버에서 생성/부여" */
    @Column(name = "merchant_uid", nullable = false, unique = true, length = 100)
    private String merchantUid;

    /** 서버 산출 금액 */
    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    private LocalDateTime paidAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = PaymentStatus.READY;
    }

    /** 결제 성공 처리: impUid 세팅 + 상태/시각 업데이트 */
    public void markPaid(String impUid) {
        this.impUid = impUid;
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    /** 결제 실패/검증 실패 처리 */
    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }
}
