package com.example.baseblock.stadium.repository;

import com.example.baseblock.stadium.entity.SeatZone;
import com.example.baseblock.stadium.entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatZoneRepository extends JpaRepository<SeatZone, Long> {

    boolean existsByStadiumAndZoneName(Stadium stadium, String zoneName);

    Optional<SeatZone> findByStadiumAndZoneName(Stadium stadium, String zoneName);
}