package com.example.baseblock.reservation.scheduler;

import com.example.baseblock.reservation.entity.Reservation;
import com.example.baseblock.common.ReservationStatus;
import com.example.baseblock.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RequiredArgsConstructor
@Component
public class SeatLockScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final ReservationRepository reservationRepository;

    @Transactional
    @Scheduled(fixedDelay = 60_000) // 1분마다
    public void releaseExpiredLocks() {
        LocalDateTime now = LocalDateTime.now(KST);
        // 간단 구현: 만료된 PENDING 전부 조회해서 EXPIRED로 전환
        // 🔧 네가 Query 메서드를 선호하면 커스텀 쿼리 추가해서 만료건만 읽어도 됨
        List<Reservation> all = reservationRepository.findAll();
        for (Reservation r : all) {
            if (r.getStatus() == ReservationStatus.PENDING &&
                    r.getLockExpiresAt() != null &&
                    r.getLockExpiresAt().isBefore(now)) {
                r.setStatus(ReservationStatus.EXPIRED);
                r.setLockExpiresAt(null);
            }
        }
    }
}
