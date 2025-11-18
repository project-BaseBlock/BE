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
        List<Reservation> all = reservationRepository.findAll();
        for (Reservation r : all) {
            if (r.getStatus() == ReservationStatus.RESERVED &&
                    r.getLockExpiresAt() != null &&
                    r.getLockExpiresAt().isBefore(now)) {
                r.setStatus(ReservationStatus.EXPIRED);
                r.setLockExpiresAt(null);
            }
        }
    }
}
