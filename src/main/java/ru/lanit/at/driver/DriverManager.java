package ru.lanit.at.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.lanit.at.Config;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static ru.lanit.at.FrameworkConstants.*;

public class DriverManager {

    private final String BROWSER_NAME;
    private final boolean PROXY_ENABLED;
    private final String HUB_URL;
    private final boolean REMOTE;

    private Config chromeDriverProperties;
    private Config geckoDriverProperties;
    private Config driverTimeoutsProperties;

    private Logger log = LogManager.getLogger(DriverManager.class);

    private ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private ProxyHandler proxyHandler;

    public DriverManager() {
        this.BROWSER_NAME = Config.getStringSystemProperty(BROWSER_VARIABLE_NAME, DEFAULT_BROWSER);
        this.PROXY_ENABLED = Config.getBooleanSystemProperty(PROXY_VARIABLE_NAME);
        this.REMOTE = Config.getBooleanSystemProperty(REMOTE_DRIVER_VARIABLE_NAME);

        if ("winium".equalsIgnoreCase(BROWSER_NAME))
            this.HUB_URL = Config.getStringSystemProperty(HUB_URL_VARIABLE_NAME, DEFAULT_WINIUM_HUB_URL);
        else this.HUB_URL = Config.getStringSystemProperty(HUB_URL_VARIABLE_NAME, DEFAULT_HUB_URL);

        loadProperties();
        defineWebDriversPath();
    }

    private void defineWebDriversPath() {
        String chromeDriverPath = System.getProperty(CHROME_DRIVER_PATH_VARIABLE_NAME);
        if (chromeDriverPath == null || chromeDriverPath.isEmpty()) {
            log.info("Chrome webdriver system variable is not set. Setting default path: " + DEFAULT_CHROME_DRIVER_PATH);
            System.setProperty(CHROME_DRIVER_PATH_VARIABLE_NAME, DEFAULT_CHROME_DRIVER_PATH);
        } else {
            log.info("Chrome webdriver system variable is detected: " + chromeDriverPath + ". Using system variable.");
        }

        String geckoDriverPath = System.getProperty(GECKO_DRIVER_PATH_VARIABLE_NAME);
        if (geckoDriverPath == null || geckoDriverPath.isEmpty()) {
            log.info("Gecko webdriver system variable is not set. Setting default path: " + DEFAULT_GECKO_DRIVER_PATH);
            System.setProperty(GECKO_DRIVER_PATH_VARIABLE_NAME, DEFAULT_GECKO_DRIVER_PATH);
        } else {
            log.info("Gecko webdriver system variable is detected: " + geckoDriverPath + ". Using system variable.");
        }
    }

    private void loadProperties() {
        chromeDriverProperties = new Config(DEFAULT_CHROME_CONFIG);
        geckoDriverProperties = new Config(DEFAULT_GECKO_CONFIG);
        driverTimeoutsProperties = new Config(DEFAULT_TIMEOUTS_CONFIG);
    }

    public WebDriver getDriver() {
        return getDriver(BROWSER_NAME);
    }

    private WebDriver getDriver(String browserName) {
        if (driver.get() == null) startBrowser(browserName);
        return driver.get();
    }

    private void startBrowser(String browserName) {
        switch (browserName.toLowerCase().trim()) {
            case "chrome":
                ChromeOptions chromeOptions = generateChromeOptions();
                logBrowserOptions("Chrome", chromeOptions);
                if (REMOTE) {
                    driver.set(generateRemoteWebDriver(chromeOptions));
                    break;
                }
                driver.set(new ChromeDriver(chromeOptions));
                break;
            case "firefox":
                FirefoxOptions firefoxOptions = generateFirefoxOptions();
                logBrowserOptions("Firefox", firefoxOptions);
                if (REMOTE) {
                    driver.set(generateRemoteWebDriver(firefoxOptions));
                    break;
                }
                driver.set(new FirefoxDriver(firefoxOptions));
                break;
            default:
                throw new FrameworkRuntimeException("Unknown driver type: " + browserName);
        }
        driver.get().manage().window().maximize();
        driver.get().manage().timeouts().implicitlyWait(driverTimeoutsProperties.getProperty(IMPLICITLY_WAIT,30), TimeUnit.SECONDS);
        driver.get().manage().timeouts().pageLoadTimeout(driverTimeoutsProperties.getProperty(PAGE_LOAD_TIMEOUT,60), TimeUnit.SECONDS);
        driver.get().manage().timeouts().setScriptTimeout(driverTimeoutsProperties.getProperty(SCRIPT_TIMEOUT,30),TimeUnit.SECONDS);
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

    private ChromeOptions generateChromeOptions() {
        ChromeOptions chromeOptions = new ChromeOptions();

        chromeOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        chromeOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

        if (PROXY_ENABLED) {
            chromeOptions.setCapability(CapabilityType.PROXY, proxyHandler.getJsonProxy());
        }

        if (!chromeDriverProperties.isEmpty()) {

            List<String> arguments = chromeDriverProperties.getProperty("arguments", false);
            List<String> extensions = chromeDriverProperties.getProperty("extensions", false);
            List<String> encodedExtensions = chromeDriverProperties.getProperty("encodedExtensions", false);
            boolean headless = chromeDriverProperties.getProperty("headless", Boolean.FALSE);
            String binaryPath = chromeDriverProperties.getProperty("binary", false);


            if (arguments != null && !arguments.isEmpty()) {
                chromeOptions.addArguments(arguments.stream().map(String::trim).toArray(String[]::new));
            }

            addExtensions(extensions, chromeOptions);
            addExtensions(encodedExtensions, chromeOptions);

            chromeOptions.setHeadless(headless);
            if (binaryPath != null && !binaryPath.isEmpty()) chromeOptions.setBinary(binaryPath);
        }

        return chromeOptions;
    }

    private void addExtensions(List<String> extensionsStrList, ChromeOptions chromeOptions) {
        if (extensionsStrList != null && !extensionsStrList.isEmpty()) {
            extensionsStrList.forEach(extensionPathStr -> {
                extensionPathStr = extensionPathStr.trim();
                File extensionFile = new File(extensionPathStr);
                if (extensionFile.exists()) {
                    log.info("Adding an extension to the chrome browser: " + extensionPathStr);
                    chromeOptions.addExtensions(extensionFile);
                } else {
                    log.error("Can't find extension with path: " + extensionPathStr);
                }
            });
        }
    }


    private FirefoxOptions generateFirefoxOptions() {
        FirefoxOptions firefoxOptions = new FirefoxOptions();

        firefoxOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        firefoxOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

        if (PROXY_ENABLED) {
            firefoxOptions.setCapability(CapabilityType.PROXY, proxyHandler.getJsonProxy());
        }

        if (!geckoDriverProperties.isEmpty()) {

//          Setting firefox binary if it's defined in config
            String binaryPath = geckoDriverProperties.getProperty("binary", false);
            String firefoxProfileName = geckoDriverProperties.getProperty("firefoxProfileName", false);
            List<String> extensions = geckoDriverProperties.getProperty("extensions", false);
            Map<String, Object> preferences = geckoDriverProperties.getProperty("preferences", false);
            Map<String, Object> capabilities = geckoDriverProperties.getProperty("capabilities", false);
            List<String> arguments = geckoDriverProperties.getProperty("arguments", false);
            boolean headless = geckoDriverProperties.getProperty("headless", Boolean.FALSE);
            boolean disableFirefoxLogging = geckoDriverProperties.getProperty("disableFirefoxLogging", Boolean.FALSE);

            if (binaryPath != null && !binaryPath.isEmpty()) firefoxOptions.setBinary(binaryPath);

//          Setting profile if it's defined in config
            if (firefoxProfileName != null && !firefoxProfileName.isEmpty()) {
                FirefoxProfile firefoxProfile = new ProfilesIni().getProfile(firefoxProfileName.trim());
                if (firefoxProfile == null)
                    throw new FrameworkRuntimeException("Could not find firefox profile with name: " + firefoxProfileName + ". Check " + DEFAULT_GECKO_CONFIG + " file.");
                addExtensions(extensions, firefoxProfile);
                firefoxOptions.setProfile(firefoxProfile);
            }

//          Setting preferences if they are defined in config
            if (preferences != null && !preferences.isEmpty())
                preferences.forEach((key, value) -> firefoxOptions.addPreference(key, value.toString().trim()));

//          Setting capabilities
            if (capabilities != null && !capabilities.isEmpty()) capabilities.forEach(firefoxOptions::setCapability);

//          Setting arguments if they are defined in config
            if (arguments != null && !arguments.isEmpty())
                arguments.forEach(argument -> firefoxOptions.addArguments(argument.trim()));

            firefoxOptions.setHeadless(headless);

            if (disableFirefoxLogging) System.setProperty("webdriver.firefox.logfile", "/dev/null");
        }
        return firefoxOptions;
    }

    private void addExtensions(List<String> extensions, FirefoxProfile firefoxProfile) {
        if (extensions != null && !extensions.isEmpty()) {
            extensions.forEach(extensionPathStr -> {
                File extensionFile = new File(extensionPathStr.trim());
                if (extensionFile.exists()) {
                    log.info("Adding an extension to the firefox browser: " + extensionPathStr);
                    firefoxProfile.addExtension(extensionFile);
                } else {
                    log.error("Can't find extension with path: " + extensionPathStr);
                }
            });
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
        return driver.get() != null;
    }

    /**
     * Closes all driver windows and destroys {@link WebDriver} instance.
     */
    public void shutdown() {
        log.info("+++ Thread "+ Thread.currentThread().getName());
        log.info("Clearing all cookies.");
        driver.get().manage().deleteAllCookies();
        log.info("Shutting down driver.");
        proxyHandler.shutDownLocalServer();
        driver.get().quit();
        driver.remove();
        log.info("Driver is closed.");
    }


    /**
     * Returns {@link JavascriptExecutor} that can execute JS in the context of the currently selected browser frame or window.
     *
     * @return Instance of {@link JavascriptExecutor} or {@code null} if {@link WebDriver} isn't initialized.
     */

    public JavascriptExecutor getJSExecutor() {
        if (driver.get() != null) {
            return (JavascriptExecutor) driver.get();
        } else {
            log.error("Драйвер не запущен! Сначала инициализируйте драйвер");
            return null;
        }
    }

    /**
     * Wrapper for {@link JavascriptExecutor#executeScript(String, Object...)}. Just casts output object into {@link String}.
     *
     * @return String output value of script executing.
     * @deprecated Use {@link ru.lanit.at.make.JSExecutor}
     */
    @Deprecated
    public String executeScript(String jsCommand, Object... args) {
        return (String) getJSExecutor().executeScript(jsCommand, args);
    }


    public void setProxyHandler(ProxyHandler proxyHandler) {
        this.proxyHandler = proxyHandler;
    }
}
