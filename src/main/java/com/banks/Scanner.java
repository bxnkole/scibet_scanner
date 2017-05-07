package com.banks;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;

/**
 * Created by banks on 2/11/17.
 */
public abstract class Scanner {
    @Getter
    @Setter
    public boolean done = false;

    public WebDriver driver = new ChromeDriver();

    public abstract void scanGames();

    public void quitDriver() {
        driver.quit();
    }

    public static void clearFile(File file) {
        try {
            FileUtils.writeStringToFile(file, "", "UTF-8", false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        System.setProperty("webdriver.chrome.driver", "/Users/banks/Downloads/chromedriver");
    }
}
