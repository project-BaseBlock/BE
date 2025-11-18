package com.example.baseblock.game.controller;

import com.example.baseblock.game.service.GameScheduleDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/demo")
public class GameScheduleDemoController {

    private final GameScheduleDemoService demoService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateDemoGame() {
        demoService.createWeeklyDemoGame();  // 주간 자동 생성 메서드 호출
        return ResponseEntity.ok("데모 경기 생성 완료 (LG vs 한화, 내일 경기)");
    }
}
