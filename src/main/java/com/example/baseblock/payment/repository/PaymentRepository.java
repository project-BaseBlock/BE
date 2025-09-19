package com.example.baseblock.payment.repository;

import com.example.baseblock.payment.entity.Payment;
import com.example.baseblock.common.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByMerchantUid(String merchantUid);
    Optional<Payment> findByImpUid(String impUid);
    Optional<Payment> findByReservation_Id(Long reservationId);

    // 멱등: 동일 예약에 대해 마지막 READY 건 재사용
    Optional<Payment> findTopByReservation_IdAndStatusOrderByIdDesc(Long reservationId, PaymentStatus status);
}
