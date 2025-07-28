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

    public List<GameSchedule> crawlMonth(int month) {
        List<GameSchedule> result = new ArrayList<>();

        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement monthDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ddlMonth")));
            Select select = new Select(monthDropdown);
            select.selectByValue(String.format("%02d", month));

            Thread.sleep(3000);

            WebElement table = driver.findElement(By.cssSelector("table#tblScheduleList > tbody"));
            List<WebElement> rows = table.findElements(By.tagName("tr"));

            LocalDate currentDate = null;

            for (int i = 0; i < rows.size(); i++) {
                List<WebElement> cols = rows.get(i).findElements(By.tagName("td"));
                if (cols.isEmpty()) continue;

                if (cols.get(0).getAttribute("class").equals("day")) {
                    String rawDate = cols.get(0).getText().split("\\(")[0];
                    int year = LocalDate.now().getYear();
                    String[] parts = rawDate.split("\\.");
                    currentDate = LocalDate.of(year, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                }

                List<WebElement> spans = rows.get(i).findElements(By.tagName("span"));
                List<String> teamNames = new ArrayList<>();
                for (WebElement span : spans) {
                    String classAttr = span.getAttribute("class");
                    if (!classAttr.contains("win") && !classAttr.contains("lose") && !classAttr.contains("draw")) {
                        String name = span.getText().trim();
                        if (!name.equals("vs") && !name.isBlank()) teamNames.add(name);
                    }
                }

                if (teamNames.size() >= 2 && i + 1 < rows.size()) {
                    String away = teamNames.get(0);
                    String home = teamNames.get(1);

                    String resultText = "";
                    Integer homeScore = null;
                    Integer awayScore = null;

                    try {
                        String win = rows.get(i).findElement(By.cssSelector("span.win")).getText();
                        String lose = rows.get(i).findElement(By.cssSelector("span.lose")).getText();
                        resultText = win + "-" + lose;
                        awayScore = Integer.parseInt(win);
                        homeScore = Integer.parseInt(lose);
                    } catch (Exception ignored) {}

                    List<WebElement> nextCols = rows.get(i + 1).findElements(By.tagName("td"));
                    String stadium = null;
                    for (WebElement td : nextCols) {
                        String text = td.getText().trim();
                        if (text.length() >= 2 && text.length() <= 10 && isKnownStadium(text)) {
                            stadium = text;
                            break;
                        }
                    }

                    if (stadium == null || stadium.isBlank()) continue;

                    result.add(GameSchedule.builder()
                            .homeTeamName(home)
                            .awayTeamName(away)
                            .stadiumName(stadium)
                            .date(currentDate)
                            .result(resultText)
                            .homeScore(homeScore)
                            .awayScore(awayScore)
                            .build());
                }
            }

            log.info("✅ {}월 크롤링 완료: {}건", month, result.size());

        } catch (Exception e) {
            log.error("❌ {}월 크롤링 실패", month, e);
        } finally {
            driver.quit();
        }

        return result;
    }

    private boolean isKnownStadium(String name) {
        return List.of("잠실", "대구", "고척", "문학", "창원", "사직", "수원", "광주", "대전").contains(name);
    }
}
