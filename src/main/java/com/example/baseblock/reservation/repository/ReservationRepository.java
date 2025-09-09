package com.example.baseblock.reservation.repository;


import com.example.baseblock.reservation.entity.Reservation;
import com.example.baseblock.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 최신 예약 1건 (예매 ID 리턴용)
    Optional<Reservation> findTopByUserOrderByIdDesc(User user);

    // 결제 완료 여부 등 조건 조회할 수도 있음 (예: 결제 전 필터링)
    // List<Reservation> findByUserAndStatus(User user, String status);

}
