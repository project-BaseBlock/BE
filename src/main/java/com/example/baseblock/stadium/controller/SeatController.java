package com.example.baseblock.stadium.controller;

import com.example.baseblock.stadium.dto.SeatResponse;
import com.example.baseblock.stadium.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    /**
     * zone / zoneName 둘 다 허용.
     * 컨트롤러에서 한글로 정규화하여 서비스에 전달.
     */
    @GetMapping
    public List<SeatResponse> getSeats(
            @RequestParam Long stadiumId,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String zoneName
    ) {
        String z = (zoneName != null && !zoneName.isBlank()) ? zoneName : zone;
        if (z == null || z.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청 파라미터 'zone' 또는 'zoneName'이 필요합니다.");
        }
        String normalized = normalizeZoneName(z); // "green"/"그린" -> "그린"
        return seatService.getSeatsByZoneAndStadium(stadiumId, normalized);
    }

    private String normalizeZoneName(String in) {
        String s = in.trim().toLowerCase();
        return switch (s) {
            case "red", "레드" -> "레드";
            case "orange", "오렌지" -> "오렌지";
            case "blue", "블루" -> "블루";
            case "navy", "네이비" -> "네이비";
            case "green", "그린" -> "그린";
            default -> in; // 확장 대비(정규화 실패 시 원문 전달)
        };
    }
}
