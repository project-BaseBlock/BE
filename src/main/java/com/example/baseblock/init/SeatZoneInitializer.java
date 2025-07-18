package com.example.baseblock.init;

import com.example.baseblock.stadium.entity.SeatZone;
import com.example.baseblock.stadium.entity.Stadium;
import com.example.baseblock.stadium.repository.SeatZoneRepository;
import com.example.baseblock.stadium.repository.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SeatZoneInitializer implements CommandLineRunner {

    private final SeatZoneRepository seatZoneRepository;
    private final StadiumRepository stadiumRepository;

    @Override
    public void run(String... args) {
        Map<String, Integer> seatZoneMap = Map.of(
          "레드", 15000,
          "오렌지", 20000,
          "블루", 20000,
          "네이비", 15000,
          "그린", 10000
        );

        List<Stadium> stadiums = stadiumRepository.findAll();

        for (Stadium stadium : stadiums) {
            for (Map.Entry<String, Integer> entry : seatZoneMap.entrySet()) {
                String zoneName = entry.getKey();
                int price = entry.getValue();

                boolean exists = seatZoneRepository.existsByStadiumAndZoneName(stadium, zoneName);
                if (!exists) {
                    seatZoneRepository.save(SeatZone.builder()
                            .stadium(stadium)
                            .zoneName(zoneName)
                            .price(price)
                            .build());
                }
            }
        }

    }
}
