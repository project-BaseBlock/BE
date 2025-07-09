package com.example.baseblock.game.dto;


import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameScheduleDto {

    private String homeTeamName;
    private String awayTeamName;
    private LocalDate date;
    private String result;     // optional: ex. "5:3"
    private String stadiumName;
    private Integer homeScore; // optional
    private Integer awayScore;

}
