package com.example.baseblock.payment.repository;

import com.example.baseblock.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByMerchantUid(String merchantUid);
    Optional<Payment> findByImpUid(String impUid);
    Optional<Payment> findByReservation_Id(Long reservationId);
}
