package com.banks;

import org.apache.commons.io.FileUtils;
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
public class ProgSportIceHockeyScanner extends Scanner {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");

    private Date date;
    private Double threshold = 60.0; //minimum percent
    private File outFile;

    public ProgSportIceHockeyScanner(Date date, Double threshold) {
        this.date = date;
        this.threshold = threshold;

        this.outFile = new File("progsports-ice-hockey-results", threshold + "%-" + simpleDateFormat.format(date) + ".txt");
    }

    public static void main(String[] args) throws ParseException {
        ProgSportIceHockeyScanner scanner = null;

        Date date;
        Double threshold;

        try {
            if (args != null && args.length == 2) {
                date = simpleDateFormat.parse(args[0]);
                threshold = Double.valueOf(args[1]);
            } else {
                String initialSelectionValue = simpleDateFormat.format(new Date()) + ",80";
                String message = "Enter date (yyMMdd) and threshold separated by comma. E.g: '19931111,70'";

                String input = (String) JOptionPane.showInputDialog(null, message, "Input", JOptionPane.INFORMATION_MESSAGE, null, null, initialSelectionValue);

                String[] split = input.split(",");
                date = simpleDateFormat.parse(split[0]);
                threshold = Double.valueOf(split[1]);
            }


            scanner = new ProgSportIceHockeyScanner(date, threshold);

            scanner.scanGames();
        } finally {
            if (scanner != null) {
                scanner.quitDriver();
            }
        }
    }



    @Override
    public void scanGames() {
        setDone(false);
        clearFile(outFile);
        System.out.println("======Scanning games for " + date + "============");
        String format = simpleDateFormat.format(date);
        driver.navigate().to("http://www.progsport.com/icehockey/bsk-predictions-" + format + ".html");
        driver.manage().window().maximize();
        driver.switchTo().window(driver.getWindowHandle());

        List<WebElement> games = driver.findElements(By.cssSelector("#anyid .odd"));

        findSweetGames(games);
        System.out.println("======Done scanning games=====");
        setDone(true);
    }

    public void findSweetGames(List<WebElement> games) {
        for (WebElement game : games) {
            try {
                String leagueCode = game.findElement(By.cssSelector("td:nth-child(2)")).getText();
                String fixture = game.findElement(By.cssSelector("td:nth-child(3)")).getText();
                String homeSuccessChanceValue = game.findElement(By.xpath("td[4]")).getText();
                String awaySuccessChanceValue = game.findElement(By.xpath("td[5]")).getText();


                StrBuilder sb = new StrBuilder();

                sb.appendln(leagueCode);

                sb.appendln("(" + date + ") " + fixture);

                boolean goodHome = Double.valueOf(homeSuccessChanceValue) > threshold;
                boolean goodAway = Double.valueOf(awaySuccessChanceValue) > threshold;
                if (goodHome || goodAway) {
                    //write to file
                    if (goodAway) {
                        sb.appendln("AWAY GAME, CAREFUL!!!!! ");
                    }
                    sb.appendln("\t Home chance:" + homeSuccessChanceValue);
                    sb.appendln("\t Away chance:" + awaySuccessChanceValue);
                    if (outFile != null) {
                        FileUtils.writeStringToFile(outFile, sb.toString(), "UTF-8", true);
                    }
                }
                System.out.println(sb);
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }

    }

}
