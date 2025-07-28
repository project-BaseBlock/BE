package com.example.baseblock.game.dto;

import com.example.baseblock.game.entity.GameSchedule;

public record GameScheduleResponse(
        Long id,
        String date,
        String homeTeam,
        String awayTeam,
        String stadiumName
) {
    public static GameScheduleResponse fromEntity(GameSchedule game) {
        return new GameScheduleResponse(
                game.getGameId(),
                game.getDate().toString(),
                game.getHome().getTeamName(),
                game.getAway().getTeamName(),
                game.getStadium().getStadiumName()
        );
    }
}
