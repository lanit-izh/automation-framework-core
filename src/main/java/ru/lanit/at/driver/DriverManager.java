package ru.lanit.at.driver;

import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.auth.AuthType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.yaml.snakeyaml.Yaml;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.io.File;
import java.io.InputStream;
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

    private Map<String, Object> chromeDriverProperties;
    private Map<String, Object> geckoDriverProperties;

    private Logger log = LogManager.getLogger(DriverManager.class);

    private WebDriver driver;

    public DriverManager() {
        this.BROWSER_NAME = getStringSystemProperty(BROWSER_VARIABLE_NAME, DEFAULT_BROWSER);
        this.PROXY_ENABLED = getBooleanSystemProperty(PROXY_VARIABLE_NAME);
        this.REMOTE = getBooleanSystemProperty(REMOTE_DRIVER_VARIABLE_NAME);

        if ("winium".equalsIgnoreCase(BROWSER_NAME))
            this.HUB_URL = getStringSystemProperty(HUB_URL_VARIABLE_NAME, DEFAULT_WINIUM_HUB_URL);
        else this.HUB_URL = getStringSystemProperty(HUB_URL_VARIABLE_NAME, DEFAULT_HUB_URL);

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
        chromeDriverProperties = readProperties(DEFAULT_CHROME_CONFIG);
        geckoDriverProperties = readProperties(DEFAULT_GECKO_CONFIG);
    }

    private Map<String, Object> readProperties(String browserConfigName) {
        InputStream input = getClass().getClassLoader().getResourceAsStream(browserConfigName);
        if (input == null) {
            log.warn("No " + browserConfigName + " config file detected." +
                    " It's strongly recommended to create file '" + browserConfigName + "' with driver configuration in 'source' directory of your project." +
                    " Creating driver with default properties.");
            return null;
        }
        Yaml yaml = new Yaml();
        return (Map<String, Object>) yaml.load(input);

    }

    private String getStringSystemProperty(String variableName, String defaultValue) {
        String variable = System.getProperty(variableName);
        if (variable == null || variable.isEmpty()) return defaultValue;
        return variable.trim();
    }

    /**
     * Tries to read system variable. By default returns false.
     *
     * @param variableName name of system variable.
     * @return {@code false} by default. True if system variable is set and {@code = true}
     */
    private boolean getBooleanSystemProperty(String variableName) {
        String variable = System.getProperty(variableName);
        return variable != null && !variable.isEmpty() && Boolean.parseBoolean(variable.trim());
    }


    public WebDriver getDriver() {
        return getDriver(BROWSER_NAME);
    }

    private WebDriver getDriver(String browserName) {
        if (driver == null) driver = startBrowser(browserName);
        return driver;
    }

    private WebDriver startBrowser(String browserName) {
        DesiredCapabilities desiredCapabilities;
        if("chrome".equalsIgnoreCase(browserName)) desiredCapabilities = DesiredCapabilities.chrome();
        else if ("firefox".equalsIgnoreCase(browserName)) desiredCapabilities = DesiredCapabilities.firefox();
        else desiredCapabilities = new DesiredCapabilities();

        if (PROXY_ENABLED) {
            Proxy proxy = startProxy();
            desiredCapabilities.setCapability(CapabilityType.PROXY, proxy);
        }

        desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        desiredCapabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);


        switch (browserName.toLowerCase().trim()) {
            case "chrome":
                generateChromeCapabilities(desiredCapabilities);
                if (REMOTE) return generateRemoteWebDriver(desiredCapabilities);
                return new ChromeDriver(desiredCapabilities);
            case "firefox":
                generateFirefoxCapabilities(desiredCapabilities);
                if (REMOTE) return generateRemoteWebDriver(desiredCapabilities);
                return new FirefoxDriver(desiredCapabilities);
            default:
                throw new FrameworkRuntimeException("Unknown driver type: " + browserName);
        }
    }

    private void generateChromeCapabilities(DesiredCapabilities desiredCapabilities) {
        desiredCapabilities.setBrowserName("chrome");
        if (chromeDriverProperties != null && !chromeDriverProperties.isEmpty()) {
            ChromeOptions chromeOptions = new ChromeOptions();

            List<String> arguments = (List<String>) chromeDriverProperties.get("arguments");
            List<String> extensions = (List<String>) chromeDriverProperties.get("extensions");
            List<String> encodedExtensions = (List<String>) chromeDriverProperties.get("encodedExtensions");

            if (arguments != null && !arguments.isEmpty()) {
                chromeOptions.addArguments(arguments.stream().map(String::trim).toArray(String[]::new));
            }

            addExtensions(extensions, chromeOptions);
            addExtensions(encodedExtensions, chromeOptions);

            desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        }
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


    private void generateFirefoxCapabilities(DesiredCapabilities desiredCapabilities) {
        desiredCapabilities.setBrowserName("firefox");
        desiredCapabilities.setCapability("marionette", true);
        desiredCapabilities.setCapability("gecko", true);

        if (geckoDriverProperties != null && !geckoDriverProperties.isEmpty()) {

            FirefoxOptions firefoxOptions = new FirefoxOptions();

//          Setting firefox binary if it's defined in config
            String binaryPath = (String) geckoDriverProperties.get("binary");
            if (binaryPath != null && !binaryPath.isEmpty()) firefoxOptions.setBinary(binaryPath);

//          Setting profile if it's defined in config
            String firefoxProfileName = (String) geckoDriverProperties.get("firefoxProfileName");
            if (firefoxProfileName != null && !firefoxProfileName.isEmpty()) {
                FirefoxProfile firefoxProfile = new ProfilesIni().getProfile(firefoxProfileName.trim());
                if (firefoxProfile == null)
                    throw new FrameworkRuntimeException("Can't find profile with name: " + firefoxProfileName);
                List<String> extensions = (List<String>) geckoDriverProperties.get("extensions");
                addExtensions(extensions, firefoxProfile);
                firefoxOptions.setProfile(firefoxProfile);
            }

//          Setting preferences if they are defined in config
            Map<String, Object> preferences = (Map<String, Object>) geckoDriverProperties.get("preferences");
            if (preferences != null && !preferences.isEmpty()) preferences.forEach((key, value) -> {
                firefoxOptions.addPreference(key, value.toString().trim());
            });

//          Setting arguments if they are defined in config
            List<String> arguments = (List<String>) geckoDriverProperties.get("arguments");
            if (arguments != null && !arguments.isEmpty()) arguments.forEach(argument -> {
                firefoxOptions.addArguments(argument.trim());
            });

            firefoxOptions.addTo(desiredCapabilities);
        }
    }

    private void addExtensions(List<String> extensions, FirefoxProfile firefoxProfile){
        if(extensions != null && !extensions.isEmpty()){
            extensions.forEach(extensionPathStr ->{
                File extensionFile = new File(extensionPathStr.trim());
                if(extensionFile.exists()){
                    log.info("Adding an extension to the chrome browser: " + extensionPathStr);
                    firefoxProfile.addExtension(extensionFile);
                } else {
                    log.error("Can't find extension with path: " + extensionPathStr);
                }
            });
        }
    }

    private Proxy startProxy() {
        BrowserMobProxyServer server = new BrowserMobProxyServer();
        server.autoAuthorization("newmos.mos.ru", "mos", "mos", AuthType.BASIC);
        server.setTrustAllServers(true);
        server.start(0);
        int port = server.getPort();
        Proxy proxy = ClientUtil.createSeleniumProxy(server);

        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            String localSocket = hostAddress + ":" + port;
            System.setProperty("proxyHost", hostAddress);
            System.setProperty("proxyPort", String.valueOf(port));
            proxy.setHttpProxy(localSocket);
            proxy.setSslProxy(localSocket);
        } catch (UnknownHostException e) {
            throw new FrameworkRuntimeException("Can't set proxy host for driver.", e);
        }

        return proxy;
    }

    private RemoteWebDriver generateRemoteWebDriver(DesiredCapabilities desiredCapabilities) {
        try {
            return new RemoteWebDriver(new URL(HUB_URL), desiredCapabilities);
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
        log.info("Закрываем драйвер");
        driver.close();
        driver.quit();
        driver = null;
    }


    /**
     * Returns {@link JavascriptExecutor} that can execute JS in the context of the currently selected browser frame or window.
     *
     * @return Instance of {@link JavascriptExecutor} or {@code null} if {@link WebDriver} isn't initialized.
     * @deprecated Use {@link ru.lanit.at.make.JSExecutor}
     */
    @Deprecated
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
