package com.example.baseblock.game.service;

import com.example.baseblock.game.entity.GameSchedule;
import com.example.baseblock.game.repository.GameScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameScheduleQueryService {

    private final GameScheduleRepository gameScheduleRepository;

    public List<GameSchedule> getByDateRange(LocalDate start, LocalDate end) {
        return gameScheduleRepository.findByDateBetweenOrderByDateAsc(start, end);
    }
}
