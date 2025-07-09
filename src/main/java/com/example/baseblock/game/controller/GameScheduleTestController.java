package com.example.baseblock.game.controller;

import com.example.baseblock.game.service.GameScheduleSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test-crawl")
public class GameScheduleTestController {

    private final GameScheduleSyncService syncService;

    @PostMapping
    public ResponseEntity<String> testCrawl() {
        syncService.manualTest();
        return ResponseEntity.ok("크롤링 테스트 완료!");
    }
}
