package com.example.baseblock.game.dto;

import com.example.baseblock.game.entity.GameSchedule;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class GameScheduleResponse {
    private Long gameId;
    private LocalDate date;
    private String homeTeam;
    private String awayTeam;

    // 추가
    private Long stadiumId;

    private String stadiumName;
    private Integer homeScore; // null 가능
    private Integer awayScore; // null 가능

    public static GameScheduleResponse fromEntity(GameSchedule gs) {
        return GameScheduleResponse.builder()
                .gameId(gs.getGameId())
                .date(gs.getDate())
                .homeTeam(gs.getHome().getTeamName())
                .awayTeam(gs.getAway().getTeamName())
                // 추가
                .stadiumId(gs.getStadium().getStadiumId())
                .stadiumName(gs.getStadium().getStadiumName())
                .homeScore(gs.getHomeScore())
                .awayScore(gs.getAwayScore())
                .build();
    }
}
