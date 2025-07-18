package com.example.baseblock.game.entity;

import lombok.*;
import jakarta.persistence.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("live_game")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveGame implements Serializable {

    @Id
    private Long scheduleId;  // GameSchedule의 ID

    private String homeTeam;

    private String awayTeam;

    private String stadium;

    private int homeScore;

    private int awayScore;

}
