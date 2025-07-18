/*
package com.example.baseblock.game.crawler;

import com.example.baseblock.game.dto.GameScheduleDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class KboApiCrawler {

    private static final String URL = "https://www.koreabaseball.com/Schedule/GameScheduleInfo.aspx";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM.dd(E)");

    public List<GameScheduleDto> crawlGameSchedules() {
        List<GameScheduleDto> result = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0")
                    .get();

            Elements rows = doc.select("table.tbl tbody tr");

            for (Element row : rows) {
                Elements tds = row.select("td");
                if (tds.size() < 6) continue; // 유효하지 않은 행

                String rawDate = tds.get(0).text().trim(); // 예: 07.16(Tue)
                String rawTime = tds.get(1).text().trim(); // 18:30
                String stadiumName = tds.get(2).text().trim(); // 잠실
                String teamsText = tds.get(3).text().trim();   // LG : 두산
                String resultText = tds.get(5).text().trim();  // 예: 5:3

                // 팀명 추출
                String[] teams = teamsText.split(":");
                if (teams.length < 2) continue;
                String homeTeam = teams[0].trim();
                String awayTeam = teams[1].trim();

                // 점수 추출
                Integer homeScore = null;
                Integer awayScore = null;
                if (resultText.contains(":")) {
                    String[] scores = resultText.split(":");
                    try {
                        homeScore = Integer.parseInt(scores[0].trim());
                        awayScore = Integer.parseInt(scores[1].trim());
                    } catch (NumberFormatException e) {
                        log.warn("점수 파싱 실패: {}", resultText);
                    }
                }

                // 날짜 파싱 (MM.dd → 오늘 연도 기준으로 보정)
                LocalDate today = LocalDate.now();
                LocalDate parsedDate = null;
                try {
                    parsedDate = LocalDate.parse(rawDate, DATE_FORMATTER.withLocale(java.util.Locale.KOREA))
                            .withYear(today.getYear());
                } catch (Exception e) {
                    log.warn("날짜 파싱 실패: {}", rawDate);
                }

                GameScheduleDto dto = GameScheduleDto.builder()
                        .homeTeamName(homeTeam)
                        .awayTeamName(awayTeam)
                        .stadiumName(stadiumName)
                        .date(parsedDate)
                        .result(resultText)
                        .homeScore(homeScore)
                        .awayScore(awayScore)
                        .build();

                result.add(dto);
            }

            log.info("✅ 크롤링 완료: {}개 일정 수집", result.size());

        } catch (Exception e) {
            log.error("❌ KBO HTML 크롤링 실패", e);
        }

        return result;
    }
}
*/