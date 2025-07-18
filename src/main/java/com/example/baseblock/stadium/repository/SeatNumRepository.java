package com.example.baseblock.stadium.repository;

import com.example.baseblock.stadium.entity.SeatNum;
import com.example.baseblock.stadium.entity.SeatZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface SeatNumRepository extends JpaRepository<SeatNum, Long> {

    // SeatZone에 속한 모든 좌석 조회
    List<SeatNum> findBySeatZone(SeatZone seatZone);

    // 좌석존(zone)과 좌석번호로 중복 확인
    boolean existsBySeatZoneAndNumber(SeatZone seatZone, String number);
}