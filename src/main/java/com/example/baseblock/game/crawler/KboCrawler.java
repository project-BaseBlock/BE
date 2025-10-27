package com.example.baseblock.game.crawler;

import com.example.baseblock.game.dto.GameSchedule;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import org.springframework.stereotype.Component;

import java.time.*;
import java.util.*;

/**
 * KBO 일정 크롤러
 * - 경기행(팀 2개 이상 감지)과 메타정보행(시간/중계/구장 등)을 분리 인식
 * - 구장은 "같은 행" 우선, 없으면 "다음 형제 행들"을 스캔하되 "다음 경기행" 이전까지만 탐색
 */
@Component
@Slf4j
public class KboCrawler {

    private static final String URL = "https://www.koreabaseball.com/Schedule/Schedule.aspx";

    public List<GameSchedule> crawlAllMonthsUpToOctober() {
        List<GameSchedule> result = new ArrayList<>();
        int currentMonth = LocalDate.now().getMonthValue();
        for (int month = currentMonth; month <= 10; month++) {
            result.addAll(crawlMonth(month));
        }
        return result;
    }

    // [기동 시 3~10월 전체]
    public List<GameSchedule> crawlAllMonthsFromMarchToOctober() {
        List<GameSchedule> result = new ArrayList<>();
        for (int month = 3; month <= 10; month++) {
            result.addAll(crawlMonth(month));
        }
        return result;
    }

    public List<GameSchedule> crawlMonth(int month) {
        List<GameSchedule> result = new ArrayList<>();

        //ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless=new"); // 필요 시 활성화
        //WebDriver driver = new ChromeDriver(options);
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // EC2용 headless 모드
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-allow-origins=*");
        options.setBinary("/usr/bin/google-chrome");

        WebDriver driver = new ChromeDriver(options);


        try {
            driver.get(URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement monthDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ddlMonth")));
            Select select = new Select(monthDropdown);
            select.selectByValue(String.format("%02d", month));

            // 렌더링 대기
            Thread.sleep(3000);

            WebElement table = driver.findElement(By.cssSelector("table#tblScheduleList > tbody"));
            List<WebElement> rows = table.findElements(By.tagName("tr"));

            LocalDate currentDate = null;

            for (int i = 0; i < rows.size(); i++) {
                List<WebElement> cols = rows.get(i).findElements(By.tagName("td"));
                if (cols.isEmpty()) continue;

                // 날짜 셀(rowspan) 대응 — 같은 날짜가 여러 경기행에 걸쳐 있을 수 있음
                if ("day".equals(cols.get(0).getAttribute("class"))) {
                    String rawDate = cols.get(0).getText().split("\\(")[0];
                    int year = LocalDate.now().getYear();
                    String[] parts = rawDate.split("\\.");
                    currentDate = LocalDate.of(year, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                }

                // [CHANGED] 팀명 추출은 보조 메서드로 분리
                List<String> teamNames = extractTeams(rows.get(i));
                if (teamNames.size() >= 2) {
                    String away = teamNames.get(0);
                    String home = teamNames.get(1);

                    // 스코어/결과
                    String resultText = "";
                    Integer homeScore = null, awayScore = null;
                    try {
                        String win = rows.get(i).findElement(By.cssSelector("span.win")).getText();
                        String lose = rows.get(i).findElement(By.cssSelector("span.lose")).getText();
                        resultText = win + "-" + lose;
                        awayScore = Integer.parseInt(win);
                        homeScore = Integer.parseInt(lose);
                    } catch (Exception ignored) {}

                    // [CHANGED] 1) 같은 행에서 먼저 구장 탐색
                    String stadium = extractStadiumFromRow(cols);

                    // [CHANGED] 2) 없으면 다음 형제 행들을 스캔 (다음 "경기행"을 만나기 전까지만)
                    if (stadium == null) {
                        int j = i + 1;
                        while (j < rows.size()) {
                            // 다음 경기행 시작되면 중단
                            if (extractTeams(rows.get(j)).size() >= 2) break;

                            List<WebElement> tds = rows.get(j).findElements(By.tagName("td"));
                            stadium = extractStadiumFromRow(tds);
                            if (stadium != null) break;
                            j++;
                        }
                    }

                    if (stadium == null || stadium.isBlank()) {
                        // 구장 식별 실패 시 스킵
                        log.debug("구장 미확인 → skip. date={}, home={}, away={}", currentDate, home, away);
                        continue;
                    }

                    GameSchedule dto = GameSchedule.builder()
                            .homeTeamName(home)
                            .awayTeamName(away)
                            .stadiumName(stadium)
                            .date(currentDate)
                            .result(resultText)
                            .homeScore(homeScore)
                            .awayScore(awayScore)
                            .build();

                    result.add(dto);

                    // [ADDED] 디버그 로깅 — 행-구장 매칭 검증용
                    log.debug("크롤링: {} | {} vs {} @ {} (result={}, score H:{} A:{})",
                            currentDate, home, away, stadium, resultText, homeScore, awayScore);
                }
            }

            log.info(" {}월 크롤링 완료: {}건", month, result.size());

        } catch (Exception e) {
            log.error(" {}월 크롤링 실패", month, e);
        } finally {
            driver.quit();
        }

        return result;
    }

    // [ADDED] 팀명 추출: 경기행 감지 기준으로 사용 (팀명이 2개 이상이면 경기행)
    private List<String> extractTeams(WebElement row) {
        List<WebElement> spans = row.findElements(By.tagName("span"));
        List<String> teamNames = new ArrayList<>();
        for (WebElement span : spans) {
            String cls = span.getAttribute("class");
            if (cls == null) continue;
            if (cls.contains("win") || cls.contains("lose") || cls.contains("draw")) continue;
            String name = span.getText().trim();
            if (!name.equals("vs") && !name.isBlank()) {
                teamNames.add(name);
            }
        }
        return teamNames;
    }

    // [ADDED] 구장 추출: 동일 행 우선, 없으면 다음 메타행들에서 토큰 분리 매칭
    private String extractStadiumFromRow(List<WebElement> tds) {
        for (WebElement td : tds) {
            String text = td.getText();
            if (text == null) continue;
            text = text.trim();
            if (text.isEmpty()) continue;

            // 정확 일치
            if (isKnownStadium(text)) return text;

            // "잠실 18:30", "고척(지연)" 같은 혼합 셀 처리
            String[] toks = text.split("[\\s,·/():-]+");
            for (String tok : toks) {
                if (isKnownStadium(tok)) return tok;
            }
        }
        return null;
    }

    private boolean isKnownStadium(String name) {
        return List.of("잠실", "대구", "고척", "문학", "창원", "사직", "수원", "광주", "대전")
                .contains(name);
    }
}
