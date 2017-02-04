package com.banks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by banks on 2/4/17.
 */
public class ScibetScanner {
    private Double threshold = 60.0; //minimum percent
    private int days = 3;
    private Long maxDateDiff = Long.valueOf(days * 24 * 60 * 60 * 1000); //3 days to milliseconds

    private WebDriver driver;

    public ScibetScanner() {
        driver = new ChromeDriver();

    }

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "/Users/banks/Downloads/chromedriver");


        ScibetScanner scanner = new ScibetScanner();

        if(args != null && args.length == 2) {
            scanner.threshold = Double.valueOf(args[0]);
            scanner.days = Integer.valueOf(args[1]);
            scanner.maxDateDiff = Long.valueOf(scanner.days * 24 * 60 * 60 * 1000);
        }
        ScheduledExecutorService s = Executors.newScheduledThreadPool(1);
        s.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                while (scanner.threshold < 100) {
                    scanner.scanTodayGames();
                    scanner.scanNearbyGames();
                    scanner.threshold += 5;
                }

                scanner.driver.quit();
            }
        }, 0, 24, TimeUnit.HOURS);

    }

    private void scanNearbyGames() {
        driver.navigate().to("http://www.scibet.com/football/");
        driver.manage().window().maximize();
        driver.switchTo().window(driver.getWindowHandle());

        List<WebElement> countries = driver.findElements(By.cssSelector("#sidebar > ul > li:nth-child(1) > ul > li > a"));
        System.out.println(countries.size());

        List<String> links = countries.stream().map(e -> e.getAttribute("href")).collect(Collectors.toList());

        File file = new File(threshold + "%-guarantee-within-" + days + "-days-games-" + new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + ".txt");
        clearFile(file);
        for (String link : links) {
            System.out.println(link);
            driver.navigate().to(link);

            List<WebElement> leagues = driver.findElements(By.className("block"));
            findSweetGamesInLeagues(leagues, file);
        }
    }

    private void clearFile(File file) {
        try {
            FileUtils.writeStringToFile(file, "", "UTF-8", false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scanTodayGames() {
        System.out.println("======Scanning today's games from home page=====");
        driver.navigate().to("http://www.scibet.com/football/");
        driver.manage().window().maximize();
        driver.switchTo().window(driver.getWindowHandle());

        List<WebElement> leagues = driver.findElements(By.className("block"));

        File file = new File(threshold + "%-guarantee-today-games-" + new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + ".txt");
        clearFile(file);
        findSweetGamesInLeagues(leagues, file);
        System.out.println("======Done scanning today's games=====");
    }

    private static String replaceWidth(String style) {
        return style.split(":")[1].replaceAll("%;", "");
    }

    /**
     * @param leagues refers to whatever has parent with class 'block'
     * @param file
     */
    public void findSweetGamesInLeagues(List<WebElement> leagues, File file) {
        for (WebElement league : leagues) {
            try {
                String leagueName = league.findElement(By.className("head")).getText();

                List<WebElement> games = league.findElements(By.cssSelector("div.content.np > table tr"));

                for (WebElement game : games) {
                    boolean h2hAvailable = StringUtils.isNotBlank(game.findElement(By.cssSelector("td:nth-child(3)")).getText());
                    Long time = Long.valueOf(game.findElement(By.cssSelector("td:nth-child(2) > span")).getAttribute("data-date") + "000");
                    Date date = new Date(time);

                    long difference = time - System.currentTimeMillis();
                    boolean tooFar = difference < 0 || difference > maxDateDiff;

                    if (h2hAvailable && !tooFar) {
                        StrBuilder sb = new StrBuilder();

                        String homeTeam = game.findElement(By.cssSelector("td:nth-child(3)")).getText();
                        String awayTeam = game.findElement(By.cssSelector("td:nth-child(5)")).getText();

                        sb.appendln(leagueName);

                        sb.appendln("(" + date + ") " + homeTeam + " vs " + awayTeam);
                        WebElement chance = game.findElement(By.cssSelector("td:nth-child(9)"));

                        WebElement homeSuccessChance = chance.findElement(By.className("bar-success"));
                        String homeSuccessChanceValue = replaceWidth(homeSuccessChance.getAttribute("style"));

                        WebElement drawChance = chance.findElement(By.className("bar-warning"));
                        String drawChanceValue = replaceWidth(drawChance.getAttribute("style"));

                        WebElement awaySuccessChance = chance.findElement(By.className("bar-danger"));
                        String awaySuccessChanceValue = replaceWidth(awaySuccessChance.getAttribute("style"));


                        if (Double.valueOf(homeSuccessChanceValue) > threshold) {
                            //write to file

                            sb.appendln("\t Home chance:" + homeSuccessChanceValue);
                            sb.appendln("\t Draw chance:" + drawChanceValue);
                            sb.appendln("\t Away chance:" + awaySuccessChanceValue);
                            if (file != null) {
                                FileUtils.writeStringToFile(new File("results", file.getName()), sb.toString(), "UTF-8", true);
                            }
                        }
                        System.out.println(sb);
                    }
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }

    }

}
