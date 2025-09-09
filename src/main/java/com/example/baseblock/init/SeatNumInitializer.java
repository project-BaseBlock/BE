package com.example.baseblock.init;

import com.example.baseblock.stadium.entity.SeatNum;
import com.example.baseblock.stadium.entity.SeatZone;
import com.example.baseblock.stadium.repository.SeatNumRepository;
import com.example.baseblock.stadium.repository.SeatZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SeatNumInitializer implements CommandLineRunner {

    private final SeatZoneRepository seatZoneRepository;
    private final SeatNumRepository seatNumRepository;

    @Override
    public void run(String... args) {

        List<SeatZone> seatZones = seatZoneRepository.findAll();

        for (SeatZone zone : seatZones) {
            String zoneName = zone.getZoneName();
            int seatCount;
            String prefix;

            switch (zoneName) {
                case "레드" -> {
                    seatCount = 100;
                    prefix = "r";
                }
                case "오렌지" -> {
                    seatCount = 100;
                    prefix = "a";
                }
                case "블루" -> {
                    seatCount = 100;
                    prefix = "b";
                }
                case "네이비" -> {
                    seatCount = 100;
                    prefix = "n";
                }
                case "그린" -> {
                    seatCount = 200;
                    prefix = "g";
                }
                default -> {
                    throw new IllegalArgumentException("알 수 없는 구역: " + zoneName);
                }
            }

            for (int i = 1; i <= seatCount; i++) {
                String seatNumber = prefix + String.format("%03d", i);

                boolean exists = seatNumRepository.existsBySeatZoneAndNumber(zone, seatNumber);
                if (!exists) {
                    seatNumRepository.save(
                            SeatNum.builder()
                                    .seatZone(zone)
                                    .number(seatNumber)
                                    .isActive(true)
                                    .build()
                    );
                }
            }
        }
    }
}