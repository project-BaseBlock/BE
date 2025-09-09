package com.example.baseblock.reservation.entity;

import com.example.baseblock.common.ReservationStatus;
import com.example.baseblock.game.entity.GameSchedule;
import com.example.baseblock.stadium.entity.SeatNum;
import com.example.baseblock.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private GameSchedule gameSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    private SeatNum seatNum;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime reservedAt;

    public int getTotalAmount() {
        if (seatNum == null || seatNum.getSeatZone() == null) {
            throw new IllegalStateException("좌석 또는 구역 정보가 없습니다.");
        }
        return seatNum.getSeatZone().getPrice();
    }

}
