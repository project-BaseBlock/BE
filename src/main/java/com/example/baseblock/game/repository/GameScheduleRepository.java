package com.example.baseblock.game.repository;

import com.example.baseblock.game.entity.GameSchedule;
import com.example.baseblock.team.entity.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GameScheduleRepository extends JpaRepository<GameSchedule, Long> {

    // === 중복 여부 체크 (기존 유지) ===
    Optional<GameSchedule> findByDateAndHome_IdAndAway_Id(LocalDate date, Long homeId, Long awayId);

    // === 날짜 범위 조회 (옵션) ===
    List<GameSchedule> findByDateBetween(LocalDate start, LocalDate end);

    // === 권장: 날짜 범위 + 정렬 + N+1 방지(EntityGraph로 즉시 로딩) ===
    @EntityGraph(attributePaths = {"home", "away", "stadium"})
    List<GameSchedule> findByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);

    // [추가] 홈팀명 + 원정팀명으로 특정 경기 삭제 (데모 경기용)
    void deleteByHomeAndAway(Team home, Team away);

    //Optional<Object> findByDateAndHome_IdAndAway_Id(LocalDate targetDate, String teamName, String teamName1);
}
