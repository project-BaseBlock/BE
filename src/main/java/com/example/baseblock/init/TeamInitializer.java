package com.example.baseblock.init;

import com.example.baseblock.stadium.entity.Stadium;
import com.example.baseblock.stadium.repository.SeatZoneRepository;
import com.example.baseblock.stadium.repository.StadiumRepository;
import com.example.baseblock.team.entity.Team;
import com.example.baseblock.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TeamInitializer implements CommandLineRunner {

    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;

    @Override
    public void run(String... args) {
        Map<String, String> teamStadiumMap = Map.of(
                "LG", "잠실",
                "두산", "잠실",
                "삼성", "대구",
                "키움", "고척",
                "SSG", "문학",
                "NC", "창원",
                "롯데", "사직",
                "KT", "수원",
                "KIA", "광주",
                "한화", "대전"
        );

        for (Map.Entry<String, String> entry : teamStadiumMap.entrySet()) {
            String teamName = entry.getKey();
            String stadiumName = entry.getValue();

            Stadium stadium = stadiumRepository.findByStadiumName(stadiumName).orElse(null);
            if (stadium == null) continue;

            boolean exists = teamRepository.existsByTeamName(teamName);
            if (!exists) {
                teamRepository.save(Team.builder()
                        .teamName(teamName)
                        .stadium(stadium)
                        .imageName(teamName.toLowerCase())
                        .build());
            }
        }
    }

}
