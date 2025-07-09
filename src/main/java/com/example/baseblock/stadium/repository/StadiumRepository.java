package com.example.baseblock.stadium.repository;

import com.example.baseblock.stadium.entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StadiumRepository extends JpaRepository<Stadium, Long> {
    Optional<Stadium> findByStadiumName(String stadiumName);
}