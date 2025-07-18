package com.example.baseblock.stadium.repository;

import com.example.baseblock.stadium.entity.SeatZone;
import com.example.baseblock.stadium.entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatZoneRepository extends JpaRepository<SeatZone, Long> {
    boolean existsByStadiumAndZoneName(Stadium stadium, String zoneName);
}