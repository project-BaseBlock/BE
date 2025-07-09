package com.example.baseblock.game.service;

import com.example.baseblock.game.crawler.KboCrawler;
import com.example.baseblock.game.dto.GameScheduleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameScheduleSyncService {

    private final KboCrawler crawler;
    private final GameScheduleService gameScheduleService;

    // ✅ 수동 테스트용 메서드 (컨트롤러에서 호출)
    public void manualTest() {
        List<GameScheduleDto> list = crawler.crawlCurrentAndNextMonthIfApplicable();
        gameScheduleService.saveOrUpdate(list);
        System.out.println("[🧪 TEST] 수동 크롤링 완료. 저장 수: " + list.size());
    }

    // ✅ 자동 크롤링 스케줄 (매일 01:00, 17:00)
    @Scheduled(cron = "0 0 1,17 * * *")
    public void syncScheduleDaily() {
        List<GameScheduleDto> list = crawler.crawlCurrentAndNextMonthIfApplicable();
        gameScheduleService.saveOrUpdate(list);
        System.out.println("[⏰ SCHEDULE] 자동 크롤링 완료. 저장 수: " + list.size());
    }
}
