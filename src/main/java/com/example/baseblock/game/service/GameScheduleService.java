package com.example.baseblock.game.service;

import com.example.baseblock.game.dto.GameScheduleDto;
import com.example.baseblock.game.entity.GameSchedule;
import com.example.baseblock.game.repository.GameScheduleRepository;
import com.example.baseblock.stadium.entity.Stadium;
import com.example.baseblock.stadium.repository.StadiumRepository;
import com.example.baseblock.team.entity.Team;
import com.example.baseblock.team.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameScheduleService {

    private final GameScheduleRepository gameScheduleRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;

    @Transactional
    public void saveOrUpdate(List<GameScheduleDto> dtoList) {
        for (GameScheduleDto dto : dtoList) {
            String homeName = dto.getHomeTeamName();
            String awayName = dto.getAwayTeamName();
            String stadiumName = dto.getStadiumName();

            if (homeName.equalsIgnoreCase("vs") || awayName.equalsIgnoreCase("vs") || homeName.isBlank() || awayName.isBlank()) {
                log.warn("❌ 잘못된 팀 이름 → home: {}, away: {}", homeName, awayName);
                continue;
            }

            Team home = teamRepository.findByTeamName(homeName)
                    .orElse(null);
            Team away = teamRepository.findByTeamName(awayName)
                    .orElse(null);
            Stadium stadium = stadiumRepository.findByStadiumName(stadiumName)
                    .orElse(null);

            if (home == null || away == null || stadium == null) {
                log.warn("❌ 팀 또는 구장 정보 없음 → home: {}, away: {}, stadium: {}", homeName, awayName, stadiumName);
                continue; // 저장하지 않고 넘어감
            }

            GameSchedule schedule = gameScheduleRepository
                    .findByDateAndHome_IdAndAway_Id(dto.getDate(), home.getId(), away.getId())
                    .orElseGet(() -> {
                        log.info("➕ 새로운 경기 일정 생성: {} vs {} on {}", homeName, awayName, dto.getDate());
                        return GameSchedule.builder()
                                .home(home)
                                .away(away)
                                .date(dto.getDate())
                                .stadium(stadium)
                                .build();
                    });

            schedule.setHomeScore(dto.getHomeScore());
            schedule.setAwayScore(dto.getAwayScore());
            schedule.setResult(dto.getResult());

            gameScheduleRepository.save(schedule);
            log.info("💾 경기 일정 저장 완료: {} vs {} on {}", homeName, awayName, dto.getDate());
        }
    }
}
