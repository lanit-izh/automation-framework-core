package ru.lanit.at.driver;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.net.MalformedURLException;
import java.net.URL;

public class DriverManager {
    private static Logger log = Logger.getLogger(DriverManager.class);
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final String DEFAULT_BROWSER = "chrome";
    private static final String DEFAULT_HUB_URL = "http://localhost:4444/wd/hub";

    static {
        System.setProperty("webdriver.gecko.driver", "src/main/resources/drivers/geckodriver.exe");
        System.setProperty("webdriver.chrome.driver", "src/main/resources/drivers/chromedriver.exe");
    }

// todo создать выбор браузера по версии и т.п.

    public static WebDriver getDriver(String browserName) {
        if (driver.get() == null) {
            driver.set(getNewDriverInstance(browserName));
        } else {
            log.error("Уже запущен драйвер " + driver.get().getClass().getSimpleName() + "!");
        }
        return driver.get();
    }

    private static WebDriver getNewDriverInstance(String browserName) {
        if ("true".equalsIgnoreCase(System.getProperty("remote"))) {
            DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setBrowserName(browserName);
            String hubUrl = System.getProperty("selenium.hub.url");
            if (hubUrl == null || hubUrl.isEmpty()) hubUrl = DEFAULT_HUB_URL;
            try {
                return new RemoteWebDriver(new URL(hubUrl), desiredCapabilities);
            } catch (MalformedURLException e) {
                throw new FrameworkRuntimeException("Exception on remote web driver initialization", e);
            }
        } else {
            return LocalDriverFactory.createInstance(browserName);
        }
    }

    public static boolean isActive() {
        return driver.get() != null;
    }

    public static void shutdown() {
        driver.get().quit();
        driver.remove();
        log.info("Закрываем драйвер");
    }

    public static WebDriver getDriver() {
        if (driver.get() == null) {
            driver.set(getNewDriverInstance(DEFAULT_BROWSER));
        }
        return driver.get();
    }

    public static JavascriptExecutor getJSExecutor() {
        if (driver.get() != null) {
            return (JavascriptExecutor) driver.get();
        } else {
            log.error("Драйвер не запущен! Сначала инициализируйте драйвер");
            return null;
        }
    }

    public static String executeScript(String jsCommand, Object... args) {
        return (String) getJSExecutor().executeScript(jsCommand, args);
    }

    public static boolean isJSActive() {
        return !executeScript("return jQuery.active").equals("0");
    }

    public static boolean isPageLoaded() {
        return executeScript("return document.readyState").equals("complete");
    }

    // страница загрузилась достаточно, чтобы с ней можно было взаимодействовать
    public static boolean isPageInterable() {
        return isPageLoaded() || executeScript("return document.readyState").equals("interactive");
    }
}
