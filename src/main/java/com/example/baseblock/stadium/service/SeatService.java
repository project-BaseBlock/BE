package com.example.baseblock.stadium.service;

import com.example.baseblock.stadium.dto.SeatResponse;
import com.example.baseblock.stadium.entity.SeatNum;
import com.example.baseblock.stadium.repository.SeatNumRepository;
import com.example.baseblock.stadium.repository.SeatZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatNumRepository seatNumRepository;
    private final SeatZoneRepository seatZoneRepository;

    /* 메서드 이름 변경 및 stadiumId 파라미터 추가
    public List<SeatResponse> getSeatsByZoneAndStadium(Long stadiumId, String zoneName) {
        return seatNumRepository.findBySeatZone_Stadium_StadiumIdAndSeatZone_ZoneName(stadiumId, zoneName)
                .stream()
                .map(SeatResponse::fromEntity)
                .toList();
    }*/

    // 수정본1
    public List<SeatResponse> getSeatsByZoneAndStadium(Long stadiumId, String zoneName) {
        return seatNumRepository.findBySeatZone_Stadium_StadiumIdAndSeatZone_ZoneName(stadiumId, zoneName)
                .stream()
                .map(SeatResponse::fromEntity)
                .toList();
    }

}