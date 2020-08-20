package ru.lanit.at.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
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
import java.util.HashMap;
import java.util.Map;
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
    private static ThreadLocal<Map<String, String>> propertiesMap = new ThreadLocal<>();
    private ProxyHandler proxyHandler;
    private boolean configLoaded;
    private MutableCapabilities additionallyCapabilities;


    public DriverManager() {
    }

    public WebDriver getDriver() {
        if (!isActive()) {
            loadConfig();
            startBrowser(BROWSER_NAME);
        }
        return driver.get();
    }

    private void startBrowser(String browserName) {
        switch (browserName.toLowerCase().trim()) {
            case "chrome":
                ChromeOptions chromeOptions = DriverOptionsBuilder.generateChromeOptions(browserConfig);
                chromeOptions.merge(additionallyCapabilities);
                logBrowserOptions("Chrome", chromeOptions);
                if (REMOTE) {
                    driver.set(generateRemoteWebDriver(chromeOptions));
                    break;
                }
                WebDriverManager.chromedriver().setup();
                driver.set(new ChromeDriver(chromeOptions));
                break;
            case "firefox":
                FirefoxOptions firefoxOptions = DriverOptionsBuilder.generateFirefoxOptions(browserConfig);
                firefoxOptions.merge(additionallyCapabilities);
                logBrowserOptions("Firefox", firefoxOptions);
                if (REMOTE) {
                    driver.set(generateRemoteWebDriver(firefoxOptions));
                    break;
                }
                WebDriverManager.firefoxdriver().setup();
                driver.set(new FirefoxDriver(firefoxOptions));
                break;
            case "opera":
                OperaOptions operaOptions = DriverOptionsBuilder.generateOperaOptions(browserConfig);
                logBrowserOptions("Opera", operaOptions);
                if (REMOTE) {
                    driver.set(generateRemoteWebDriver(operaOptions));
                    break;
                }
                WebDriverManager.operadriver().setup();
                driver.set(new OperaDriver(operaOptions));
                break;
            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.merge(DriverOptionsBuilder.getCapabilities(browserConfig));
                edgeOptions.merge(additionallyCapabilities);
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
                ieOptions.merge(additionallyCapabilities);
                logBrowserOptions("Internet explore", ieOptions);
                if (REMOTE) {
                    driver.set(generateRemoteWebDriver(ieOptions));
                    break;
                }
                driver.set(new InternetExplorerDriver(ieOptions));
                break;
            case "remote":
                MutableCapabilities mutableCapabilities = DriverOptionsBuilder.getCapabilities(browserConfig);
                logBrowserOptions("Remote", mutableCapabilities);
                mutableCapabilities.merge(additionallyCapabilities);
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
        } catch (WebDriverException ex) {
            log.error("Error while stopping driver: " + ex.getMessage());
            driver.remove();
        }
    }


    /**
     * Set browser instance
     */
    public void setDriver(WebDriver value) {
        log.info("Driver was set: " + value);
        driver.set(value);
    }


    /**
     * Add  additional browser capabilities, the method must be called before the browser starts
     */
    public void addCapabilities(MutableCapabilities capabilities) {
        if (isActive()) {
            log.info("Additional capabilities cannot be installed because the browser is active");
        } else {
            log.info("Additional will be installed  : " + capabilities.toString());
            additionallyCapabilities = capabilities;
        }
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


    private void loadConfig() {
        if (!configLoaded) {
            if(propertiesMap.get().isEmpty()) {
                this.BROWSER_NAME = Config.getStringSystemProperty(BROWSER_VARIABLE_NAME, DEFAULT_BROWSER);
                this.REMOTE = Config.getBooleanSystemProperty(REMOTE_DRIVER_VARIABLE_NAME);
                this.HUB_URL = Config.getStringSystemProperty(HUB_URL_VARIABLE_NAME, DEFAULT_HUB_URL);
                this.browserConfig = new Config(Config.getStringSystemProperty(BROWSER_CONFIG, DEFAULT_CHROME_CONFIG));
            } else {
                this.BROWSER_NAME = propertiesMap.get().get("browser");
                this.REMOTE = Boolean.parseBoolean(propertiesMap.get().get("remote"));
                this.HUB_URL = propertiesMap.get().get("hub_url");
                this.browserConfig = new Config(propertiesMap.get().get("hub_url"));
            }
            this.driverTimeoutsProperties = new Config(DEFAULT_TIMEOUTS_CONFIG);
            configLoaded = true;
        }
    }

    public static void setPropertiesMap(Map<String, String> map) {
        propertiesMap.set(map);
    }
}
