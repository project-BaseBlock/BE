package com.example.baseblock.game.entity;

import com.example.baseblock.stadium.entity.Stadium;
import com.example.baseblock.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;


import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team home;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    private Team away;

    private LocalDate date;

    private String result; // optional: 예: "5:3", null 가능

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    private Integer homeScore;

    private Integer awayScore;

    public Long getId() {
        return this.gameId;
    }

}
