package ru.lanit.at.driver;

import net.lightbody.bmp.proxy.ProxyServer;
import org.apache.log4j.Logger;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;

public class LocalDriverFactory {
    private static Logger log = Logger.getLogger(LocalDriverFactory.class);

    static WebDriver createInstance(String browserName) {
        WebDriver driver;
        startProxy(7878);

        switch (browserName.toLowerCase()) {
            case "firefox":
                FirefoxOptions ffo = CapabilitiesManager.getFirefoxOptions();
                FirefoxProfile profile = new FirefoxProfile();
                profile.setPreference("plugin.default.state", 2);
                profile.setPreference("focusmanager.testmode", false);
                profile.setPreference("browser.tabs.remote.autostart.2", false);
                profile.setPreference("layout.spellcheckDefault", 0);
                profile.setPreference("devtools.selfxss.count", 1500);
                profile.setPreference("dom.webnotifications.enabled", false);
                profile.setPreference("network.proxy.socks", "127.0.0.1");
                profile.setPreference("network.proxy.socks_port", 7878);
                driver = new FirefoxDriver(profile);
                break;

            case "chrome":
                ChromeOptions chromeOptions = CapabilitiesManager.getChromeOptions();
                chromeOptions.addArguments("--proxy-server=http://127.0.0.1:7878");
                driver = new ChromeDriver(chromeOptions);
                break;

            default:
                return null;
        }

        driver.manage().window().maximize();
        log.info("Создан драйвер для " + browserName);
        return driver;

    }

    private static void startProxy(int port) {
        ProxyServer bmp = new ProxyServer(port);
        try {
            bmp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        bmp.autoBasicAuthorization("", "mos", "mos");
    }
}
