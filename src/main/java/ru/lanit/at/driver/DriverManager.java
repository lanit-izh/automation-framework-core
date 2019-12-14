package ru.lanit.at.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.lanit.at.Config;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static ru.lanit.at.FrameworkConstants.*;

public class DriverManager {

    private String BROWSER_NAME;

    private String HUB_URL;
    private boolean REMOTE;


    private Config driverTimeoutsProperties;
    private Config browserConfig;

    private Logger log = LogManager.getLogger(DriverManager.class);

    private ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private ProxyHandler proxyHandler;

    public DriverManager() {
        this.BROWSER_NAME = Config.getStringSystemProperty(BROWSER_VARIABLE_NAME, DEFAULT_BROWSER);
        this.REMOTE = Config.getBooleanSystemProperty(REMOTE_DRIVER_VARIABLE_NAME);
        this.HUB_URL = Config.getStringSystemProperty(HUB_URL_VARIABLE_NAME, DEFAULT_HUB_URL);
        this.browserConfig = new Config(Config.getStringSystemProperty(BROWSER_CONFIG, DEFAULT_CHROME_CONFIG));
        driverTimeoutsProperties = new Config(DEFAULT_TIMEOUTS_CONFIG);
    }

    public WebDriver getDriver() {
        if (!isActive()) startBrowser(BROWSER_NAME);
        return driver.get();
    }

    private void startBrowser(String browserName) {
        switch (browserName.toLowerCase().trim()) {
            case "chrome":
                ChromeOptions chromeOptions = DriverOptionsBuilder.generateChromeOptions(browserConfig);
                logBrowserOptions("Chrome", chromeOptions);
                if (REMOTE) {
                    driver.set(generateRemoteWebDriver(chromeOptions));
                    break;
                }
                driver.set(new ChromeDriver(chromeOptions));
                break;
            case "firefox":
                FirefoxOptions firefoxOptions = DriverOptionsBuilder.generateFirefoxOptions(browserConfig);
                logBrowserOptions("Firefox", firefoxOptions);
                if (REMOTE) {
                    driver.set(generateRemoteWebDriver(firefoxOptions));
                    break;
                }
                driver.set(new FirefoxDriver(firefoxOptions));
                break;
            case "opera":
                OperaOptions operaOptions = DriverOptionsBuilder.generateOperaOptions(browserConfig);
                logBrowserOptions("Opera", operaOptions);
                if (REMOTE) {
                    driver.set(generateRemoteWebDriver(operaOptions));
                    break;
                }
                driver.set(new OperaDriver(operaOptions));
                break;
            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.merge(DriverOptionsBuilder.getCapabilities(browserConfig));
                logBrowserOptions("Edge", edgeOptions);
                if (REMOTE) {
                    driver.set(generateRemoteWebDriver(edgeOptions));
                    break;
                }
                driver.set(new EdgeDriver(edgeOptions));
                break;

            case "internet explore":
                InternetExplorerOptions ieOptions = new InternetExplorerOptions();
                ieOptions.merge(DriverOptionsBuilder.getCapabilities(browserConfig));
                logBrowserOptions("Internet explore", ieOptions);
                if (REMOTE) {
                    driver.set(generateRemoteWebDriver(ieOptions));
                    break;
                }
                driver.set(new InternetExplorerDriver(ieOptions));
                break;
            case "remote":
                MutableCapabilities mutableCapabilities= DriverOptionsBuilder.getCapabilities(browserConfig);
                logBrowserOptions("Remote", mutableCapabilities);
                driver.set(generateRemoteWebDriver(DriverOptionsBuilder.getCapabilities(browserConfig)));
                break;

            default:
                throw new FrameworkRuntimeException("Unknown driver type: " + browserName);
        }
        if (System.getProperty("windowSizeMaximize", "true").equalsIgnoreCase("true")){
            driver.get().manage().window().maximize();
        }
        if (System.getProperty("timeouts", "true").equalsIgnoreCase("true")) {
            Integer implWait = driverTimeoutsProperties.getProperty(IMPLICITLY_WAIT, 30);
            System.setProperty("webdriver.timeouts.implicitlywait", implWait.toString());
            driver.get().manage().timeouts().implicitlyWait(implWait, TimeUnit.SECONDS);
            driver.get().manage().timeouts().pageLoadTimeout(driverTimeoutsProperties.getProperty(PAGE_LOAD_TIMEOUT, 60), TimeUnit.SECONDS);
            driver.get().manage().timeouts().setScriptTimeout(driverTimeoutsProperties.getProperty(SCRIPT_TIMEOUT, 30), TimeUnit.SECONDS);
        }
    }

    private RemoteWebDriver generateRemoteWebDriver(MutableCapabilities mutableCapabilities) {
        try {
            RemoteWebDriver remoteWebDriver = new RemoteWebDriver(new URL(HUB_URL), mutableCapabilities);
            remoteWebDriver.setFileDetector(new LocalFileDetector());
            return remoteWebDriver;
        } catch (MalformedURLException e) {
            throw new FrameworkRuntimeException("Exception while creating remote webdriver. Check hub url: " + HUB_URL, e);
        }
    }

    /**
     * Determines weather current {@link WebDriver} is null or not.
     *
     * @return true if driver is not null.
     */
    public boolean isActive() {
        return driver.get() != null && ((RemoteWebDriver) driver.get()).getSessionId() != null;
    }

    /**
     * Closes all driver windows and destroys {@link WebDriver} instance.
     */
    public void shutdown() {
        try {
            log.info("Clearing all cookies.");
            driver.get().manage().deleteAllCookies();
            log.info("Shutting down driver.");
            proxyHandler.shutDownLocalServer();
            driver.get().quit();
            log.info("Driver is closed.");
        }catch (WebDriverException ex){
            log.error("Ошибка при закрытии драйвер: "+ex.getMessage());
            driver.remove();
        }
    }


    /**
     * Set browser instance
     */
    public void setDriver(WebDriver value) {
        log.info("Установлен драйвер: "+value);
        driver.set(value);
    }




    public void setProxyHandler(ProxyHandler proxyHandler) {
        this.proxyHandler = proxyHandler;
    }


    private void logBrowserOptions(String browserName, MutableCapabilities options) {
        log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        log.info("Starting {} browser with options:", browserName);
        log.info(options);
        log.info("Proxy: {}", Config.getBooleanSystemProperty(PROXY_VARIABLE_NAME));
        log.info("Remote: {}", Config.getBooleanSystemProperty(REMOTE_DRIVER_VARIABLE_NAME));
        log.info("Hub url: {}", Config.getStringSystemProperty(HUB_URL_VARIABLE_NAME, HUB_URL));
        log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }
}
