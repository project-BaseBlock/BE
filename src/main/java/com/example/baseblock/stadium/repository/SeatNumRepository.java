package com.example.baseblock.stadium.repository;

import com.example.baseblock.stadium.entity.SeatNum;
import com.example.baseblock.stadium.entity.SeatZone;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatNumRepository extends JpaRepository<SeatNum, Long> {

    long countBySeatZone(SeatZone seatZone);

    boolean existsBySeatZoneAndNumber(SeatZone seatZone, String number);

    // 구장ID + 구역명으로 좌석 조회
    List<SeatNum> findBySeatZone_Stadium_StadiumIdAndSeatZone_ZoneName(Long stadiumId, String zoneName);

    // 구장ID + 구역명 + 좌석번호들로 조회
    List<SeatNum> findBySeatZone_ZoneNameAndSeatZone_Stadium_StadiumIdAndNumberIn(
            String zoneName, Long stadiumId, List<String> seatNumbers
    );

    // 동시성 제어용: 특정 좌석들 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sn FROM SeatNum sn " +
            "WHERE sn.seatZone.zoneName = :zoneName " +
            "AND sn.seatZone.stadium.stadiumId = :stadiumId " +
            "AND sn.number IN :seatNumbers")
    List<SeatNum> findForUpdateByZoneAndStadiumAndNumbers(
            @Param("zoneName") String zoneName,
            @Param("stadiumId") Long stadiumId,
            @Param("seatNumbers") List<String> seatNumbers
    );

    int countBySeatZoneAndNumberIn(SeatZone seatZone, List<String> numbers);
}
