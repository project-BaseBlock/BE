package com.example.baseblock.payment.entity;

import com.example.baseblock.common.PaymentStatus;
import com.example.baseblock.reservation.entity.Reservation;
import com.example.baseblock.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_merchant_uid", columnNames = "merchant_uid"),
                @UniqueConstraint(name = "uk_payment_imp_uid", columnNames = "imp_uid")
        },
        indexes = {
                @Index(name = "ix_payment_reservation_id", columnList = "reservation_id")
        }
)
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "merchant_uid", nullable = false, length = 64)
    private String merchantUid;   // 멱등 키(UNIQUE)

    @Column(name = "imp_uid", length = 64)
    private String impUid;        // 아임포트 결제 UID(성공 시 세팅)

    @Column(nullable = false)
    private int amount;           // 서버산출 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentStatus status; // READY / PAID / FAILED / REFUNDED

    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "payment", fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    /** 결제 성공 */
    public void markPaid(String impUid) {
        this.impUid = impUid;
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    /** 결제 실패 */
    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }
}
