package com.example.baseblock.stadium.repository;

import com.example.baseblock.stadium.entity.SeatNum;
import com.example.baseblock.stadium.entity.SeatZone;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/*
8**************************************************************************************************
******************************************분기점*******************************************
***************************************************************************************************
*/

import java.util.List;
public interface SeatNumRepository extends JpaRepository<SeatNum, Long> {

    // SeatZone에 속한 모든 좌석 조회
    List<SeatNum> findBySeatZone(SeatZone seatZone);

    // 좌석존(zone)과 좌석번호로 중복 확인
    boolean existsBySeatZoneAndNumber(SeatZone seatZone, String number);

    //zone 이름으로 좌석 리스트 조회
    List<SeatNum> findBySeatZone_ZoneName(String zoneName);

    // 좌석 번호 목록으로 좌석들 조회
    List<SeatNum> findByNumberIn(List<String> seatNumbers);

    //[SeatService에서 사용할 메서드]
    // 구역명 + 구장ID로 좌석 리스트 조회 (추가)
    List<SeatNum> findBySeatZone_Stadium_StadiumIdAndSeatZone_ZoneName(Long stadiumId, String zoneName);

    // 구역명 + 구장ID + 좌석번호 리스트로 조회 (메서드 네이밍 기반)
    List<SeatNum> findBySeatZone_ZoneNameAndSeatZone_Stadium_StadiumIdAndNumberIn(
            String zoneName,
            Long stadiumId,
            List<String> seatNumbers
    );

    // 이 메서드를 추가해야 합니다!
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sn FROM SeatNum sn WHERE sn.seatZone.zoneName = :zoneName AND sn.seatZone.stadium.stadiumId = :stadiumId AND sn.number IN :seatNumbers")
    List<SeatNum> findForUpdateByZoneAndStadiumAndNumbers(@Param("zoneName") String zoneName, @Param("stadiumId") Long stadiumId, @Param("seatNumbers") List<String> seatNumbers);
}