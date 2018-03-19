package ru.lanit.at.driver;

import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.auth.AuthType;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.winium.DesktopOptions;
import org.openqa.selenium.winium.WiniumDriver;
import org.testng.Assert;
import ru.lanit.at.FrameworkConstants;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Для запуска WINIUM используются следующие переменные окружения:
 * winium (true/false)
 * winium.app.path (path to executable)
 * winium.hub.url (http://localhost:9999 as default)
 */
public class DriverManager {
    public static BrowserMobProxyServer server;

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
     *
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
            server = new BrowserMobProxyServer();
            server.autoAuthorization("newmos.mos.ru","mos","mos", AuthType.BASIC);
            server.setTrustAllServers(true);
            server.start(0);
            int port = server.getPort();
            Proxy proxy = ClientUtil.createSeleniumProxy(server);
            try {
                proxy.setHttpProxy(InetAddress.getLocalHost().getHostAddress() + ":" + port);
            } catch (UnknownHostException e) {
                throw new FrameworkRuntimeException("Can't set proxy host for driver.", e);
            }
            DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
            if(browserName.equalsIgnoreCase("chrome")) {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--always-authorize-plugins=true");
                options.addArguments("--ignore-certificate-errors");
                options.addExtensions(new File("src/main/resources/drivers/1.2.1_0.crx"));
                options.addArguments("--disable-blink-features=BlockCredentialedSubresources");
                options.addArguments("--start-maximized");
                desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
            }
            if(browserName.equalsIgnoreCase("firefox")) {
                ProfilesIni profileIni = new ProfilesIni();
                FirefoxProfile profile = profileIni.getProfile("qa");
                profile.setPreference("plugin.default.state", 2);
                profile.setPreference("focusmanager.testmode", false);
                profile.setPreference("browser.tabs.remote.autostart.2", false);
                profile.setPreference("layout.spellcheckDefault", 0);
                desiredCapabilities.setCapability("marionette", true);
                desiredCapabilities.setCapability("gecko", true);
                profile.setPreference("devtools.selfxss.count", 1500);
                profile.setPreference("dom.webnotifications.enabled", false);
                desiredCapabilities.setCapability(FirefoxDriver.PROFILE, profile);
            }
            desiredCapabilities.setCapability(CapabilityType.PROXY, proxy);
            desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            desiredCapabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
            desiredCapabilities.setBrowserName(browserName);
            String hubUrl = System.getProperty("selenium.hub.url");
            if (hubUrl == null || hubUrl.isEmpty()) hubUrl = DEFAULT_HUB_URL;
            try {
                driver = new RemoteWebDriver(new URL(hubUrl), desiredCapabilities);
            } catch (MalformedURLException e) {
                throw new FrameworkRuntimeException("Exception on remote web driver initialization", e);
            }
            if(!browserName.equalsIgnoreCase("chrome")) {
                driver.manage().window().maximize();
            }
            return driver;
        } else {
            return LocalDriverFactory.createInstance(browserName);
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
        driver.quit();
        driver = null;
        log.info("Закрываем драйвер");
    }

    /**
     * Returns lazy-initialized {@link WebDriver} instance, using default browser name = {@value DEFAULT_BROWSER}
     *
     * @return Instance of {@link WebDriver}
     */
    public WebDriver getDriver() {
        if (driver == null) {
            driver = getNewDriverInstance(getBrowserName());
        }
        return driver;
    }

    private String getBrowserName() {
        String browserSystemVariableStr = System.getProperty(FrameworkConstants.BROWSER_VARIABLE_NAME);
        if (browserSystemVariableStr == null || browserSystemVariableStr.isEmpty()) return DEFAULT_BROWSER;
        return browserSystemVariableStr;
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
     */
    public String executeScript(String jsCommand, Object... args) {
        return (String) getJSExecutor().executeScript(jsCommand, args);
    }

    /**
     * Determines if JavaScript is active in selected frame or window. Executes JavaScript that shows if jQuery is active.
     *
     * @return {@code true} if {@code jQuery.active != 0}.
     */
    public boolean isJSActive() {
        return !executeScript("return jQuery.active").equals("0");
    }

    /**
     * Executes JavaScript to determine if selected browser frame or window is loaded completely.
     *
     * @return {@code true} if {@code document.readyState = complete}
     */
    public boolean isPageLoaded() {
        return executeScript("return document.readyState").equals("complete");
    }

    // страница загрузилась достаточно, чтобы с ней можно было взаимодействовать

    /**
     * Determines if the selected browser frame or window is interactive or not by executing JavaScript.
     *
     * @return {@code true} if {@code document.readyState = interactive}
     */
    public boolean isPageInteractive() {
        return isPageLoaded() || executeScript("return document.readyState").equals("interactive");
    }
}
