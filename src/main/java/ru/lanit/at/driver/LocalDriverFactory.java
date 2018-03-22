package ru.lanit.at.driver;

import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.auth.AuthType;
import org.apache.log4j.Logger;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LocalDriverFactory {
    public static final String LOCALHOST = "127.0.0.1";
    private static Logger log = Logger.getLogger(LocalDriverFactory.class);

    public static BrowserMobProxyServer server;

    static WebDriver createInstance(String browserName) {
        WebDriver driver;

        server = new BrowserMobProxyServer();
        server.autoAuthorization("newmos.mos.ru","mos","mos", AuthType.BASIC);
        server.setTrustAllServers(true);
        server.blacklistRequests("mc\\.yandex\\.ru", 200);
        server.blacklistRequests("stat\\.sputnik\\.ru", 200);
        server.start(0);
        int port = server.getPort();
        Proxy proxy = ClientUtil.createSeleniumProxy(server);
        settingProxy(port, proxy, true);

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
                DesiredCapabilities seleniumCapabilities = new DesiredCapabilities();
                if(System.getProperty("proxy","true").equalsIgnoreCase("true")) {
                    seleniumCapabilities.setCapability(CapabilityType.PROXY, proxy);
                }
                seleniumCapabilities.setCapability(FirefoxDriver.PROFILE,profile);
                seleniumCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                seleniumCapabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
                driver = new FirefoxDriver(seleniumCapabilities);
                break;

            case "chrome":
                ChromeOptions chromeOptions = CapabilitiesManager.getChromeOptions();
                DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
                capabilities.setCapability (CapabilityType.ACCEPT_SSL_CERTS, true);
                capabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
                capabilities.setCapability("proxy", proxy);
                driver = new ChromeDriver(capabilities);
                break;

            default:
                return null;
        }

        driver.manage().window().maximize();
        log.info("Создан драйвер для " + browserName);
        return driver;

    }

    static void settingProxy(int port, Proxy proxy, Boolean local) {
        try {
            String hostAddress;
            if(local) {
                hostAddress = LOCALHOST;
            } else {
                hostAddress = InetAddress.getLocalHost().getHostAddress();
            }
            String localSocket = hostAddress + ":" + port;
            System.setProperty("proxyHost",hostAddress);
            System.setProperty("proxyPort", String.valueOf(port));
            proxy.setHttpProxy(localSocket);
            proxy.setSslProxy(localSocket);
        } catch (UnknownHostException e) {
            throw new FrameworkRuntimeException("Can't set proxy host for driver.", e);
        }
    }
}
