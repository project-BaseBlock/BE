package com.example.baseblock.game.controller;

import com.example.baseblock.game.dto.GameScheduleResponse;
import com.example.baseblock.game.service.GameScheduleQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class GameScheduleController {

    private final GameScheduleQueryService service;

    // 예) /games?start=2025-10-25&end=2025-11-02
    @GetMapping
    public List<GameScheduleResponse> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getByDateRange(start, end).stream()
                .map(GameScheduleResponse::fromEntity)
                .toList();
    }
}
