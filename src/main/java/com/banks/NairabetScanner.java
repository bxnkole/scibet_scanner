package com.banks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.swing.JOptionPane;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by banks on 2/4/17.
 */
public class NairabetScanner extends Scanner {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Date date = new Date();
    private Double threshold = 60.0; //minimum difference
    private File outFile;


    public NairabetScanner() {
        this.outFile = new File("nairabet-results", threshold + "%-" + simpleDateFormat.format(date) + ".txt");
    }

    public static void main(String[] args) throws ParseException {
        NairabetScanner scanner = new NairabetScanner();

        scanner.scanGames();
    }


    @Override
    public void scanGames() {
        setDone(false);
        clearFile(outFile);
        System.out.println("======Scanning games for " + date + "============");
        String format = simpleDateFormat.format(date);
        driver.navigate().to("https://www.nairabet.com/UK/homepage");
//        driver.manage().window().maximize();
        driver.switchTo().window(driver.getWindowHandle());

        List<WebElement> leagues = driver.findElements(By.cssSelector("span.sidemenulink-span"));
        for (WebElement league : leagues) {
            String leagueText = league.getText();
            if(leagueText.contains("FOOTBALL")) {
                league.click();
                sleep(3000L);
                List<WebElement> leagues2 = driver.findElements(By.cssSelector("span.sidemenulink-span"));
                for (WebElement webElement : leagues2) {
                    System.out.println(webElement.getText());
                }
            }
        }
//        findSweetGamesInLeagues(leagues);
//        System.out.println("======Done scanning games=====");
//        setDone(true);
    }

    private void sleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String replaceWidth(String style) {
        return style.split(":")[1].replaceAll("%;", "");
    }

    /**
     * @param leagues refers to whatever has parent with class 'block'
     */
    public void findSweetGamesInLeagues(List<WebElement> leagues) {
        for (WebElement league : leagues) {
            try {
                String leagueName = league.findElement(By.className("head")).getText();

                List<WebElement> games = league.findElements(By.cssSelector("div.content.np > table tr"));

                for (WebElement game : games) {
                    boolean h2hAvailable = StringUtils.isNotBlank(game.findElement(By.cssSelector("td:nth-child(4)")).getText());
                    Long time = Long.valueOf(game.findElement(By.cssSelector("td:nth-child(2) > span")).getAttribute("data-date") + "000");
                    Date date = new Date(time);

                    if (h2hAvailable) {
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


                        boolean goodHome = Double.valueOf(homeSuccessChanceValue) > threshold;
                        boolean goodAway = Double.valueOf(awaySuccessChanceValue) > threshold;
                        if (goodHome || goodAway) {
                            //write to file
                            if (goodAway) {
                                sb.appendln("AWAY GAME, CAREFUL!!!!! ");
                            }
                            sb.appendln("\t Home chance:" + homeSuccessChanceValue);
                            sb.appendln("\t Draw chance:" + drawChanceValue);
                            sb.appendln("\t Away chance:" + awaySuccessChanceValue);
                            if (outFile != null) {
                                FileUtils.writeStringToFile(outFile, sb.toString(), "UTF-8", true);
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
