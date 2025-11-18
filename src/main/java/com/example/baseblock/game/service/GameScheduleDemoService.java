package com.example.baseblock.game.service;

import com.example.baseblock.game.entity.GameSchedule;
import com.example.baseblock.game.repository.GameScheduleRepository;
import com.example.baseblock.stadium.entity.Stadium;
import com.example.baseblock.stadium.repository.SeatNumRepository;
import com.example.baseblock.stadium.repository.StadiumRepository;
import com.example.baseblock.team.entity.Team;
import com.example.baseblock.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GameScheduleDemoService {

    private final GameScheduleRepository repo;
    private final StadiumRepository stadiumRepo;
    private final SeatNumRepository seatNumRepo;
    private final TeamRepository teamRepo;

    /**
     * 매주 월요일 00:00 실행
     * - 화요일~일요일(6일치) 경기 생성
     * - LG vs 한화 (잠실)
     * - 기존 경기 유지, 중복은 생성 안 함
     */
    @Scheduled(cron = "0 0 0 * * MON") // 매주 월요일 00:00
    @Transactional
    public void createWeeklyDemoGame() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.with(DayOfWeek.TUESDAY); // 이번 주 화요일
        LocalDate endDate = today.with(DayOfWeek.SUNDAY);    // 이번 주 일요일

        Stadium stadium = stadiumRepo.findByStadiumName("잠실")
                .orElseThrow(() -> new IllegalStateException("잠실 구장 없음"));

        Team homeTeam = teamRepo.findByTeamName("lg")
                .orElseThrow(() -> new IllegalStateException("lg 팀 없음"));
        Team awayTeam = teamRepo.findByTeamName("한화")
                .orElseThrow(() -> new IllegalStateException("한화 팀 없음"));

        // 잠실 좌석 초기화 (중복 방지용)
        seatNumRepo.resetSeatsByStadium(stadium.getStadiumId());

        int createdCount = 0;

        // 화요일~일요일 6일치 경기 생성
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            boolean exists = repo.findByDateAndHome_IdAndAway_Id(date, homeTeam.getId(), awayTeam.getId())
                    .isPresent();
            if (exists) {
                System.out.println("[DemoGame] ⚠ 이미 존재: " + date);
                continue;
            }

            GameSchedule demo = GameSchedule.builder()
                    .home(homeTeam)
                    .away(awayTeam)
                    .stadium(stadium)
                    .date(date)
                    .result(null)
                    .homeScore(null)
                    .awayScore(null)
                    .build();

            repo.save(demo);
            createdCount++;
            System.out.println("[DemoGame] 🆕 경기 생성: " + date);
        }

        System.out.printf("[DemoGame] ✅ 이번 주 경기 %d개 생성 완료 (%s ~ %s)%n",
                createdCount, startDate, endDate);
    }
}
