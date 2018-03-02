package ru.lanit.at.driver;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class LocalDriverFactory {
    private static Logger log = Logger.getLogger(LocalDriverFactory.class);

    static WebDriver createInstance(String browserName) {
        WebDriver driver;

        switch (browserName.toLowerCase()) {
            case "firefox":
                FirefoxOptions ffo = CapabilitiesManager.getFirefoxOptions();
                driver = new FirefoxDriver(ffo);
                break;

            case "chrome":
                ChromeOptions chromeOptions = CapabilitiesManager.getChromeOptions();
                driver = new ChromeDriver(chromeOptions);
                break;

            default:
                return null;
        }

        driver.manage().window().maximize();
        log.info("Создан драйвер для " + browserName);
        return driver;

    }
}
