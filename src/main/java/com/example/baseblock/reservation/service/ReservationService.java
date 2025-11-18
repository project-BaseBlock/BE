package com.example.baseblock.reservation.service;

import com.example.baseblock.common.ReservationStatus;
import com.example.baseblock.game.entity.GameSchedule;
import com.example.baseblock.game.repository.GameScheduleRepository;
import com.example.baseblock.reservation.dto.ReservationRequest;
import com.example.baseblock.reservation.entity.Reservation;
import com.example.baseblock.reservation.repository.ReservationRepository;
import com.example.baseblock.stadium.entity.SeatNum;
import com.example.baseblock.stadium.repository.SeatNumRepository;
import com.example.baseblock.user.entity.User;
import com.example.baseblock.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final GameScheduleRepository gameScheduleRepository;
    private final SeatNumRepository seatNumRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createReservation(ReservationRequest request, String email) {
        GameSchedule game = gameScheduleRepository.findById(request.getGameId())
                .orElseThrow(() -> new IllegalArgumentException("해당 경기가 존재하지 않습니다."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        // 비관적 락(Pessimistic Lock)을 사용하여 좌석을 조회합니다.
        List<SeatNum> seats = seatNumRepository.findForUpdateByZoneAndStadiumAndNumbers(
                request.getZoneName(),
                request.getStadiumId(),
                request.getSeatNumbers()
        );

        if (seats.size() != request.getSeatNumbers().size()) {
            throw new IllegalArgumentException("유효하지 않은 좌석 번호가 포함되어 있습니다.");
        }

        for (SeatNum seat : seats) {
            if (!seat.isActive()) {
                throw new IllegalStateException("이미 예약된 좌석이 포함되어 있습니다: " + seat.getNumber());
            }
            // 예약 완료 후 좌석 상태를 비활성화(isActive = false)로 변경합니다.
            seat.setActive(false);
        }

        // 배치 저장
        List<Reservation> saved = reservationRepository.saveAll(
                seats.stream()
                        .map(seat -> Reservation.builder()
                                .user(user)
                                .gameSchedule(game)
                                .seatNum(seat)
                                .status(ReservationStatus.RESERVED)
                                .reservedAt(LocalDateTime.now()) // 예약 시간 추가
                                .build())
                        .toList()
        );

        // 첫 번째 예약 ID만 리턴
        return saved.stream()
                .findFirst()
                .map(Reservation::getId)
                .orElseThrow(() -> new RuntimeException("예약 저장 실패"));
    }
}