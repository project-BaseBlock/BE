package com.example.baseblock.init;

import com.example.baseblock.stadium.entity.Stadium;
import com.example.baseblock.stadium.repository.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StadiumInitializer implements CommandLineRunner {

    private final StadiumRepository stadiumRepository;

    @Override
    public void run(String... args) {

        List<String> stadiums = List.of("잠실", "대구", "고척", "문학", "창원", "사직", "광주", "대전", "수원");

        for (String name : stadiums) {
            stadiumRepository .findByStadiumName(name)
                    .orElseGet(() -> stadiumRepository.save(Stadium.builder()
                            .stadiumName(name)
                            .build()));
        }

    }

}
