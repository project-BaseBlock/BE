package com.example.baseblock.game.crawler;

import com.example.baseblock.game.dto.GameScheduleDto;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class KboCrawler {

    private static final String URL = "https://www.koreabaseball.com/Schedule/Schedule.aspx";

    public List<GameScheduleDto> crawlMonth(int month) {
        List<GameScheduleDto> result = new ArrayList<>();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(URL);

            Select select = new Select(driver.findElement(By.id("ddlMonth")));
            select.selectByValue(String.valueOf(month));

            Thread.sleep(1000); // JS 로딩 대기

            WebElement table = driver.findElement(By.className("tbl-type06"));
            String[] lines = table.getText().split("\n");

            String currentDate = null;

            for (String line : lines) {
                String[] tokens = line.trim().split(" ");

                if (tokens.length == 0) continue;

                if (tokens[0].matches("\\d{2}\\.\\d{2}\\(.*\\)")) {
                    currentDate = tokens[0];
                    continue;
                }

                if (currentDate != null && tokens.length >= 3) {
                    String[] teams = tokens[0].split("vs");
                    if (teams.length != 2) continue;

                    result.add(GameScheduleDto.builder()
                            .homeTeamName(teams[0].trim())
                            .awayTeamName(teams[1].trim())
                            .date(LocalDate.parse("2025-" + currentDate.substring(0, 2) + "-" + currentDate.substring(3, 5)))
                            .stadiumName(tokens[tokens.length - 2])
                            .result(null) // 추후 필요 시 파싱
                            .build());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return result;
    }

    public List<GameScheduleDto> crawlCurrentAndNextMonthIfApplicable() {
        List<GameScheduleDto> result = new ArrayList<>();
        int currentMonth = LocalDate.now().getMonthValue();

        if (currentMonth < 3 || currentMonth > 9) {
            return result; // 10~2월은 크롤링 안 함
        }

        result.addAll(crawlMonth(currentMonth));

        if (currentMonth < 9) {
            result.addAll(crawlMonth(currentMonth + 1));
        }

        return result;
    }

}
