package com.example.baseblock.reservation.repository;

import com.example.baseblock.reservation.entity.Reservation;
import com.example.baseblock.user.entity.User;
import com.example.baseblock.common.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findTopByUserOrderByIdDesc(User user);

    // 🔐 /payments/ready에서 동시요청 방지용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") Long id);

    // (스케줄러용, 참고) 만료 락 조회
    List<Reservation> findByStatusAndLockExpiresAtBefore(ReservationStatus status, LocalDateTime time);
}
