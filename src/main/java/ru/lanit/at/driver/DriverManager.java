package ru.lanit.at.driver;

import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.auth.AuthType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import static ru.lanit.at.FrameworkConstants.*;

public class DriverManager {

    private final String BROWSER_NAME;
    private final boolean PROXY_ENABLED;
    private final String HUB_URL;
    private final boolean REMOTE;

    private Config chromeDriverProperties;
    private Config geckoDriverProperties;
    private Config proxyProperties;

    private Logger log = LogManager.getLogger(DriverManager.class);

    private WebDriver driver;
    private Proxy proxy;

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
        proxyProperties = new Config(DEFAULT_PROXY_CONFIG);
    }

    public WebDriver getDriver() {
        return getDriver(BROWSER_NAME);
    }

    private WebDriver getDriver(String browserName) {
        if (driver == null) driver = startBrowser(browserName);
        return driver;
    }

    private WebDriver startBrowser(String browserName) {
        WebDriver driver;
        switch (browserName.toLowerCase().trim()) {
            case "chrome":
                ChromeOptions chromeOptions = generateChromeOptions();
                if (REMOTE) {
                    driver = generateRemoteWebDriver(chromeOptions);
                    break;
                }
                driver = new ChromeDriver(chromeOptions);
                break;
            case "firefox":
                FirefoxOptions firefoxOptions = generateFirefoxOptions();
                if (REMOTE) {
                    driver = generateRemoteWebDriver(firefoxOptions);
                    break;
                }
                driver = new FirefoxDriver(firefoxOptions);
                break;
            default:
                throw new FrameworkRuntimeException("Unknown driver type: " + browserName);
        }
        driver.manage().window().maximize();
        return driver;
    }

    private ChromeOptions generateChromeOptions() {
        ChromeOptions chromeOptions = new ChromeOptions();

        chromeOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        chromeOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

        if (PROXY_ENABLED) {
            chromeOptions.setProxy(getProxy());
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

        firefoxOptions.setCapability("marionette", true);
        firefoxOptions.setCapability("gecko", true);
        firefoxOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        firefoxOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

        if (PROXY_ENABLED) {
            firefoxOptions.setProxy(getProxy());
        }

        if (!geckoDriverProperties.isEmpty()) {

//          Setting firefox binary if it's defined in config
            String binaryPath = geckoDriverProperties.getProperty("binary", false);
            String firefoxProfileName = geckoDriverProperties.getProperty("firefoxProfileName", false);
            List<String> extensions = geckoDriverProperties.getProperty("extensions", false);
            Map<String, Object> preferences = geckoDriverProperties.getProperty("preferences", false);
            List<String> arguments = geckoDriverProperties.getProperty("arguments", false);
            boolean headless = geckoDriverProperties.getProperty("headless", Boolean.FALSE);
            boolean disableFirefoxLogging = geckoDriverProperties.getProperty("disableFirefoxLogging", Boolean.FALSE);
            boolean isVncEnabled = geckoDriverProperties.getProperty("enableVNC", Boolean.FALSE);


            firefoxOptions.setCapability("enableVNC", isVncEnabled);

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
            if (preferences != null && !preferences.isEmpty()) preferences.forEach((key, value) -> firefoxOptions.addPreference(key, value.toString().trim()));

//          Setting arguments if they are defined in config
            if (arguments != null && !arguments.isEmpty()) arguments.forEach(argument -> firefoxOptions.addArguments(argument.trim()));

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

    private Proxy getProxy() {
        if (proxy == null) proxy = startProxy();
        return proxy;
    }

    private Proxy startProxy() {
        if (proxyProperties == null || proxyProperties.isEmpty())
            throw new FrameworkRuntimeException("Proxy properties are not defined. Please initialize properties in "
                    + DEFAULT_PROXY_CONFIG + " file.");

        Proxy proxy;

        boolean startLocal = proxyProperties.getProperty("startLocal", Boolean.FALSE);
        int port = proxyProperties.getProperty("port", 0);

        if (startLocal) {
            String domainForAutoAuthorization = proxyProperties.getProperty("domainForAutoAuthorization", false);
            String authUsername = proxyProperties.getProperty("authUsername", false);
            String authPassword = proxyProperties.getProperty("authPassword", false);
            String authType = proxyProperties.getProperty("authType", "BASIC");
            boolean trustAllServers = proxyProperties.getProperty("trustAllServers", Boolean.FALSE);


            BrowserMobProxyServer server = new BrowserMobProxyServer();
            if (domainForAutoAuthorization != null)
                server.autoAuthorization(domainForAutoAuthorization, authUsername, authPassword, AuthType.valueOf(authType.toUpperCase().trim()));
            server.setTrustAllServers(trustAllServers);
            server.start(port);
            if (port == 0) port = server.getPort();
            proxy = ClientUtil.createSeleniumProxy(server);

            try {
                String localHostAddress = REMOTE ? InetAddress.getLocalHost().getHostAddress() : "127.0.0.1";
                String localSocket = localHostAddress + ":" + port;
                System.setProperty("proxyHost", localHostAddress);
                System.setProperty("proxyPort", String.valueOf(port));
                proxy.setHttpProxy(localSocket);
                proxy.setSslProxy(localSocket);
            } catch (UnknownHostException e) {
                throw new FrameworkRuntimeException("Can't set proxy host for driver.", e);
            }

        } else {
            String host = proxyProperties.getProperty("host", true);
            String socksProxy = proxyProperties.getProperty("socksProxy", false);
            String socksUsername = proxyProperties.getProperty("socksUsername", false);
            String socksPassword = proxyProperties.getProperty("socksPassword", false);

            String socket = host + ":" + port;
            proxy = new Proxy();
            proxy.setSslProxy(socket);
            proxy.setHttpProxy(socket);
            proxy.setSocksProxy(socksProxy);
            proxy.setSocksUsername(socksUsername);
            proxy.setSocksPassword(socksPassword);
        }

        return proxy;
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
        return driver != null;
    }

    /**
     * Closes all driver windows and destroys {@link WebDriver} instance.
     */
    public void shutdown() {
        log.info("Clearing all cookies.");
        driver.manage().deleteAllCookies();
        log.info("Shutting down driver.");
        driver.quit();
        driver = null;
        log.info("Driver is closed.");
    }


    /**
     * Returns {@link JavascriptExecutor} that can execute JS in the context of the currently selected browser frame or window.
     *
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
     *
     * @return String output value of script executing.
     * @deprecated Use {@link ru.lanit.at.make.JSExecutor}
     */
    @Deprecated
    public String executeScript(String jsCommand, Object... args) {
        return (String) getJSExecutor().executeScript(jsCommand, args);
    }


}
