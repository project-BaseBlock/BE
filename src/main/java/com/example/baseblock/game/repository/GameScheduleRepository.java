package com.example.baseblock.game.repository;

import com.example.baseblock.game.entity.GameSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface GameScheduleRepository extends JpaRepository<GameSchedule, Long> {

    // 동일한 경기 일정이 있는지 확인
    Optional<GameSchedule> findByDateAndHome_IdAndAway_Id(LocalDate date, Long homeId, Long awayId);

}
