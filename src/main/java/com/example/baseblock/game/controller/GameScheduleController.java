package com.example.baseblock.game.controller;

import com.example.baseblock.game.dto.GameScheduleResponse;
import com.example.baseblock.game.service.GameScheduleQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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

    private final GameScheduleQueryService gameScheduleQueryService;

    @GetMapping
    public ResponseEntity<List<GameScheduleResponse>> getSchedules(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        List<GameScheduleResponse> schedules = gameScheduleQueryService.getSchedules(start, end);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/reservable")
    public ResponseEntity<List<GameScheduleResponse>> getReservableSchedules() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(6);

        List<GameScheduleResponse> schedules = gameScheduleQueryService.getSchedules(start, end);
        return ResponseEntity.ok(schedules);
    }
}
