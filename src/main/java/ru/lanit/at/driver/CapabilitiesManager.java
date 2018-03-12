package ru.lanit.at.driver;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

public class CapabilitiesManager {

    public static ChromeOptions getChromeOptions() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--always-authorize-plugins=true");
        chromeOptions.addArguments("--ignore-certificate-errors");
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
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("plugin.default.state", 2);
        profile.setPreference("focusmanager.testmode", false);
        profile.setPreference("browser.tabs.remote.autostart.2", false);
        profile.setPreference("layout.spellcheckDefault", 0);
        DesiredCapabilities dc = DesiredCapabilities.firefox();
        dc.setCapability("marionette", true);
        dc.setCapability("gecko", true);
        profile.setPreference("devtools.selfxss.count", 1500);
        profile.setPreference("dom.webnotifications.enabled", false);
        dc.setCapability(FirefoxDriver.PROFILE, profile);
        return dc;

    }
}
