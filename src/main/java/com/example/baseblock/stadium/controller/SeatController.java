package com.example.baseblock.stadium.controller;

import com.example.baseblock.stadium.dto.SeatResponse;
import com.example.baseblock.stadium.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // stadiumId 파라미터 추가
    @GetMapping
    public List<SeatResponse> getSeats(
            @RequestParam Long stadiumId,
            @RequestParam String zone
    ) {
        // 서비스 호출 시 stadiumId도 함께 전달
        return seatService.getSeatsByZoneAndStadium(stadiumId, zone);
    }
}