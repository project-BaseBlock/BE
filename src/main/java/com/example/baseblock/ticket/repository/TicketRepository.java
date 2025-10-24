package com.example.baseblock.ticket.repository;

import com.example.baseblock.ticket.entity.Ticket;
import com.example.baseblock.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // 멱등/존재 확인: 경기 + 좌석 조합
    Optional<Ticket> findByGameSchedule_IdAndSeatNum_Id(Long gameScheduleId, Long seatNumId);
    boolean existsByGameSchedule_IdAndSeatNum_Id(Long gameScheduleId, Long seatNumId);
    long countByGameSchedule_IdAndSeatNum_Id(Long gameScheduleId, Long seatNumId);

    // 마이페이지 목록 (최신 발급순)
    Page<Ticket> findByUserOrderByIssuedAtDesc(User user, Pageable pageable);

    // 상세 조회(소유권)
    Optional<Ticket> findByIdAndUser(Long id, User user);

    // 결제 직후 조회(예약ID + 사용자로 최신 1건)
    Optional<Ticket> findFirstByReservation_IdAndUser_IdOrderByIdDesc(Long reservationId, Long userId);

    // ✅ [ADDED] 클레임/정합 처리를 위한 비관적 락 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.id = :id")
    Optional<Ticket> findByIdForUpdate(@Param("id") Long id);
}
