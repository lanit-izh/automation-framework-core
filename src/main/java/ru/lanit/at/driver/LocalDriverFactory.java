package ru.lanit.at.driver;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

public class LocalDriverFactory {
    private static Logger log = Logger.getLogger(LocalDriverFactory.class);

    static WebDriver createInstance(String browserName) {
        WebDriver driver;

        switch (browserName.toLowerCase()) {
            case "firefox":
                FirefoxOptions ffo = CapabilitiesManager.getFirefoxOptions();
                FirefoxProfile profile = new FirefoxProfile();
                profile.setPreference("devtools.selfxss.count", 1500);
                profile.setPreference("dom.webnotifications.enabled", false);
                driver = new FirefoxDriver(profile);
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
