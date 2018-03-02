package ru.lanit.at.driver;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

public class CapabilitiesManager {

    public static ChromeOptions getChromeOptions() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--always-authorize-plugins=true");
        chromeOptions.addExtensions(new File("src/main/resources/drivers/1.2.1_0.crx"));
        chromeOptions.addArguments("--disable-blink-features=BlockCredentialedSubresources");
        return chromeOptions;
    }

    public static FirefoxOptions getFirefoxOptions() {
        FirefoxOptions ffo = new FirefoxOptions();
        ffo.setLogLevel(java.util.logging.Level.OFF);
        ffo.addPreference("acceptInsecureCerts", true);
        return ffo;
    }

    public static DesiredCapabilities getChromeCapabibilities() {
        DesiredCapabilities dc = DesiredCapabilities.chrome();
        dc.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
        return dc;
    }

    public static DesiredCapabilities getFirefoxCapabilities() {
        return DesiredCapabilities.firefox();
    }
}
