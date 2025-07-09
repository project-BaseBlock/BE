package com.example.baseblock.payment.entity;

import com.example.baseblock.common.PaymentStatus;
import com.example.baseblock.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    private String impUid;        // 아임포트 결제 고유 ID
    private String merchantUid;   // 상점 주문번호 (프론트에서 생성한 값)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime paidAt;

}
