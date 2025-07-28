package com.example.baseblock.game.repository;

import com.example.baseblock.game.entity.GameSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GameScheduleRepository extends JpaRepository<GameSchedule, Long> {

    // 동일한 경기 일정이 있는지 확인
    Optional<GameSchedule> findByDateAndHome_IdAndAway_Id(LocalDate date, Long homeId, Long awayId);

    // 날짜 범위로 경기 일정 조회
    List<GameSchedule> findByDateBetween(LocalDate start, LocalDate end);
}
