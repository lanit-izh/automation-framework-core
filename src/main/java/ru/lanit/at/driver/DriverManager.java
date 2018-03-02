package ru.lanit.at.driver;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.winium.DesktopOptions;
import org.openqa.selenium.winium.WiniumDriver;
import org.testng.Assert;
import ru.lanit.at.FrameworkConstants;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Для запуска WINIUM используются следующие переменные окружения:
 * winium (true/false)
 * winium.app.path (path to executable)
 * winium.hub.url (http://localhost:9999 as default)
 */
public class DriverManager {
    private Logger log = Logger.getLogger(DriverManager.class);
    private WebDriver driver;
    private static final String DEFAULT_BROWSER = "chrome";
    private static final String DEFAULT_HUB_URL = "http://localhost:4444/wd/hub";
    private static final String DEFAULT_WINIUM_HUB_URL = "http://localhost:9999";

    static {
        System.setProperty("webdriver.gecko.driver", "src/main/resources/drivers/geckodriver.exe");
        System.setProperty("webdriver.chrome.driver", "src/main/resources/drivers/chromedriver.exe");
    }

// todo создать выбор браузера по версии и т.п.

    /**
     * Returns lazy-initialized {@link WebDriver} instance by given browser name.
     * @param browserName available browser names: 'winium', 'remote', 'chrome', 'firefox' and other browser names, supported by {@link DesiredCapabilities}
     * @return Instance of {@link WebDriver}
     */
    public WebDriver getDriver(String browserName) {
        if (driver == null) {
            driver = getNewDriverInstance(browserName);
        } else {
            log.error("Уже запущен драйвер " + driver.getClass().getSimpleName() + "!");
        }
        return driver;
    }

    private WebDriver getNewDriverInstance(String browserName) {
        if ("true".equalsIgnoreCase(System.getProperty("winium"))) {
            String appPath = System.getProperty("winium.app.path");
            DesktopOptions option = new DesktopOptions();
            option.setApplicationPath(appPath);
            // TODO: пока фабрика настраивается дефолтными значениями, возможности сконфигурять DesiredCapabilities нет.
            option.setDebugConnectToRunningApp(false);
            option.setLaunchDelay(2);
            String hubUrl = System.getProperty("winium.hub.url");
            if (hubUrl == null || hubUrl.isEmpty()) hubUrl = DEFAULT_WINIUM_HUB_URL;
            try {
                return new WiniumDriver(new URL(hubUrl), option);
            } catch (MalformedURLException e) {
                Assert.fail("Could not connect to driver!");
            }
        }
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

    /**
     * Determines weather current {@link WebDriver} is null or not.
     * @return true if driver is not null.
     */
    public boolean isActive() {
        return driver != null;
    }

    /**
     * Closes all driver windows and destroys {@link WebDriver} instance.
     */
    public void shutdown() {
        driver.quit();
        driver = null;
        log.info("Закрываем драйвер");
    }

    /**
     * Returns lazy-initialized {@link WebDriver} instance, using default browser name = {@value DEFAULT_BROWSER}
     * @return Instance of {@link WebDriver}
     */
    public WebDriver getDriver() {
        if (driver == null) {
            driver = getNewDriverInstance(getBrowserName());
        }
        return driver;
    }

    private String getBrowserName(){
        String browserSystemVariableStr = System.getProperty(FrameworkConstants.BROWSER_VARIABLE_NAME);
        if( browserSystemVariableStr == null || browserSystemVariableStr.isEmpty()) return DEFAULT_BROWSER;
        return browserSystemVariableStr;
    }

    /**
     * Returns {@link JavascriptExecutor} that can execute JS in the context of the currently selected browser frame or window.
     * @return Instance of {@link JavascriptExecutor} or {@code null} if {@link WebDriver} isn't initialized.
     */
    public JavascriptExecutor getJSExecutor() {
        if (driver != null) {
            return (JavascriptExecutor) driver;
        } else {
            log.error("Драйвер не запущен! Сначала инициализируйте драйвер");
            return null;
        }
    }

    /**
     * Wrapper for {@link JavascriptExecutor#executeScript(String, Object...)}. Just casts output object into {@link String}.
     * @return String output value of script executing.
     */
    public String executeScript(String jsCommand, Object... args) {
        return (String) getJSExecutor().executeScript(jsCommand, args);
    }

    /**
     * Determines if JavaScript is active in selected frame or window. Executes JavaScript that shows if jQuery is active.
     * @return {@code true} if {@code jQuery.active != 0}.
     */
    public boolean isJSActive() {
        return !executeScript("return jQuery.active").equals("0");
    }

    /**
     * Executes JavaScript to determine if selected browser frame or window is loaded completely.
     * @return {@code true} if {@code document.readyState = complete}
     */
    public boolean isPageLoaded() {
        return executeScript("return document.readyState").equals("complete");
    }

    // страница загрузилась достаточно, чтобы с ней можно было взаимодействовать

    /**
     * Determines if the selected browser frame or window is interactive or not by executing JavaScript.
     * @return {@code true} if {@code document.readyState = interactive}
     */
    public boolean isPageInteractive() {
        return isPageLoaded() || executeScript("return document.readyState").equals("interactive");
    }
}
