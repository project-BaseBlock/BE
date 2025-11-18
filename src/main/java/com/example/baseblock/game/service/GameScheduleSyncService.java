package com.example.baseblock.game.service;

import com.example.baseblock.game.crawler.KboCrawler;
import com.example.baseblock.game.dto.GameSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameScheduleSyncService {

    private final KboCrawler crawler;
    private final GameScheduleService gameScheduleService;

    // 수동 테스트용 메서드: 전체 경기 크롤링
    public void manualTest() {
        // crawler.crawlAllMonthsUpToOctober() 대신 3월부터 크롤링하는 메서드를 호출
        List<GameSchedule> list = crawler.crawlAllMonthsFromMarchToOctober();
        gameScheduleService.saveOrUpdate(list);
        System.out.println("✅ 수동 3월~10월 크롤링 완료: " + list.size() + "개 저장됨");
    }

    // 자동 크롤링 스케줄 (매일 01:00, 17:00)
    @Scheduled(cron = "0 0 1,17 * * *")
    public void syncScheduleDaily() {
        List<GameSchedule> list = crawler.crawlAllMonthsUpToOctober();
        gameScheduleService.saveOrUpdate(list);

        System.out.println("[⏰ SCHEDULE] 자동 크롤링 완료. 저장 수: " + list.size());
    }
}
