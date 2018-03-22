package ru.lanit.at.driver;

import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.auth.AuthType;
import org.apache.log4j.Logger;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.net.MalformedURLException;
import java.net.URL;

import static ru.lanit.at.FrameworkConstants.*;

public class RemoteDriverFactory {
    private static Logger log = Logger.getLogger(RemoteDriverFactory.class);

    public static BrowserMobProxyServer server;

    static RemoteWebDriver createInstance(String browserName) {

        DesiredCapabilities capability;
        RemoteWebDriver driver;

        server = new BrowserMobProxyServer();
        server.autoAuthorization("newmos.mos.ru","mos","mos", AuthType.BASIC);
        server.setTrustAllServers(true);
        server.start(0);
        int port = server.getPort();
        Proxy proxy = ClientUtil.createSeleniumProxy(server);

        switch (browserName.toLowerCase()) {
            case "firefox":
                capability = CapabilitiesManager.getFirefoxCapabilities();
                capability.setCapability(CapabilityType.PROXY, proxy);
                capability.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                capability.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
                break;
            case "chrome":
                capability = CapabilitiesManager.getChromeCapabibilities();
                capability.setCapability(CapabilityType.PROXY, proxy);
                capability.setCapability (CapabilityType.ACCEPT_SSL_CERTS, true);
                capability.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
                capability.setCapability(ChromeOptions.CAPABILITY, CapabilitiesManager.getChromeOptions());
                break;
            default:
                return null;
        }

        if (isVNCEnabled()) {
            capability.setCapability(ENABLE_VNC, true);
            log.info("VNC подключено");
        } else {
            log.info("VNC не подключен");
        }

        if (isVideoEnabled()) {
            capability.setCapability(ENABLE_VIDEO, true);
            log.info("Запись видео включена.");
        } else {
            log.info("Запись видео не включена.");
        }

        URL hubUrl = getHubUrl();
        driver = new RemoteWebDriver(hubUrl, capability);
        log.info("Создан Remote драйвер " + browserName + " для " + hubUrl.toString());

//        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().window().maximize();
        log.info("Размер окна браузера установлен на 1920х1080");

        driver.setFileDetector(new LocalFileDetector());

        return driver;
    }

    private static URL getHubUrl() {
        String url = System.getProperty(HUB_URL_VARIABLE_NAME, DEFAULT_HUB_URL);
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            log.error("Неверно задан адрес хаба selenoid/selenium: " + url);
            throw new FrameworkRuntimeException(e);
        }
    }

    private static boolean isVideoEnabled() {
        try {
            return System.getProperty(ENABLE_VIDEO).equalsIgnoreCase("true");
        } catch (NullPointerException npe) {
            return false;
        }

    }

    private static boolean isVNCEnabled() {
        try {
            return System.getProperty(ENABLE_VNC).equalsIgnoreCase("true");
        } catch (NullPointerException npe) {
            return false;
        }
    }
}