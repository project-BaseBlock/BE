package com.example.baseblock.game.controller;

import com.example.baseblock.game.service.GameScheduleSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
public class GameScheduleTestController {

    private final GameScheduleSyncService syncService;

    @PostMapping("/manual-test")
    public String runManualTest() {
        syncService.manualTest();
        return "✅ 수동 테스트 완료";
    }
}
