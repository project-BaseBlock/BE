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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameScheduleService {

    private final GameScheduleRepository gameScheduleRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;

    @Transactional
    public void saveOrUpdate(List<GameScheduleDto> dtoList) {
        for (GameScheduleDto dto : dtoList) {
            Team home = teamRepository.findByTeamName(dto.getHomeTeamName())
                    .orElseThrow(() -> new IllegalArgumentException("홈팀 없음: " + dto.getHomeTeamName()));

            Team away = teamRepository.findByTeamName(dto.getAwayTeamName())
                    .orElseThrow(() -> new IllegalArgumentException("원정팀 없음: " + dto.getAwayTeamName()));

            Stadium stadium = stadiumRepository.findByStadiumName(dto.getStadiumName())
                    .orElseThrow(() -> new IllegalArgumentException("구장 없음: " + dto.getStadiumName()));

            GameSchedule schedule = gameScheduleRepository
                    .findByDateAndHome_IdAndAway_Id(dto.getDate(), home.getId(), away.getId())
                    .orElse(GameSchedule.builder()
                            .home(home)
                            .away(away)
                            .date(dto.getDate())
                            .stadium(stadium)
                            .build());

            schedule.setHomeScore(dto.getHomeScore());
            schedule.setAwayScore(dto.getAwayScore());
            schedule.setResult(dto.getResult());

            gameScheduleRepository.save(schedule);
        }
    }
}
