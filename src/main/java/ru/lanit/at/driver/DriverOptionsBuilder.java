package ru.lanit.at.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.CapabilityType;
import ru.lanit.at.Config;
import ru.lanit.at.context.Context;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.lanit.at.FrameworkConstants.PROXY_VARIABLE_NAME;


class DriverOptionsBuilder {
    private static final Logger LOG = LogManager.getLogger(DriverOptionsBuilder.class);

    static ChromeOptions generateChromeOptions(Config chromeDriverProperties) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        if (!chromeDriverProperties.isEmpty()) {
            List<String> arguments = chromeDriverProperties.getProperty("arguments", false);
            List<String> extensions = chromeDriverProperties.getProperty("extensions", false);
            List<String> encodedExtensions = chromeDriverProperties.getProperty("encodedExtensions", false);
            boolean headless = chromeDriverProperties.getProperty("headless", Boolean.FALSE);
            String binaryPath = chromeDriverProperties.getProperty("binary", false);

            if (arguments != null && !arguments.isEmpty()) {
                chromeOptions.addArguments(arguments.stream().map(String::trim).toArray(String[]::new));
            }

            getExtensions(extensions).forEach(chromeOptions::addExtensions);
            getExtensions(encodedExtensions).forEach(chromeOptions::addExtensions);
            chromeOptions.setHeadless(headless);
            if (binaryPath != null && !binaryPath.isEmpty()) chromeOptions.setBinary(binaryPath);
        }

        chromeOptions.merge(getCapabilities(chromeDriverProperties));
        return chromeOptions;

    }

    static FirefoxOptions generateFirefoxOptions(Config geckoDriverProperties) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        if (!geckoDriverProperties.isEmpty()) {

//          Setting firefox binary if it's defined in config
            String binaryPath = geckoDriverProperties.getProperty("binary", false);
            String firefoxProfileName = geckoDriverProperties.getProperty("firefoxProfileName", false);
            List<String> extensions = geckoDriverProperties.getProperty("extensions", false);
            Map<String, Object> preferences = geckoDriverProperties.getProperty("preferences", false);
            List<String> arguments = geckoDriverProperties.getProperty("arguments", false);
            boolean headless = geckoDriverProperties.getProperty("headless", Boolean.FALSE);
            boolean disableFirefoxLogging = geckoDriverProperties.getProperty("disableFirefoxLogging", Boolean.FALSE);

            if (binaryPath != null && !binaryPath.isEmpty()) firefoxOptions.setBinary(binaryPath);

//          Setting profile if it's defined in config
            if (firefoxProfileName != null && !firefoxProfileName.isEmpty()) {
                FirefoxProfile firefoxProfile = new ProfilesIni().getProfile(firefoxProfileName.trim());
                if (firefoxProfile == null) {
                    throw new FrameworkRuntimeException("Could not find firefox profile with name: " + firefoxProfileName + ". Check config file.");
                }
                getExtensions(extensions).forEach(firefoxProfile::addExtension);
                firefoxOptions.setProfile(firefoxProfile);
            }

//          Setting preferences if they are defined in config
            if (preferences != null && !preferences.isEmpty())
                preferences.forEach((key, value) -> firefoxOptions.addPreference(key, value.toString().trim()));

//          Setting arguments if they are defined in config
            if (arguments != null && !arguments.isEmpty())
                arguments.forEach(argument -> firefoxOptions.addArguments(argument.trim()));

            firefoxOptions.setHeadless(headless);

            if (disableFirefoxLogging) System.setProperty("webdriver.firefox.logfile", "/dev/null");
        }
        firefoxOptions.merge(getCapabilities(geckoDriverProperties));
        return firefoxOptions;
    }


    static OperaOptions generateOperaOptions(Config operaDriverProperties) {
        WebDriverManager.operadriver().setup();
        OperaOptions operaOptions = new OperaOptions();
        if (!operaDriverProperties.isEmpty()) {
//          Setting  binary if it's defined in config
            String binaryPath = operaDriverProperties.getProperty("binary", false);
            List<String> extensions = operaDriverProperties.getProperty("extensions", false);
            List<String> arguments = operaDriverProperties.getProperty("arguments", false);

            if (binaryPath != null && !binaryPath.isEmpty()) operaOptions.setBinary(binaryPath);
//          Setting profile if it's defined in config
            getExtensions(extensions).forEach(operaOptions::addExtensions);

//          Setting arguments if they are defined in config
            if (arguments != null && !arguments.isEmpty()) {
                arguments.forEach(argument -> operaOptions.addArguments(argument.trim()));
            }
        }
        operaOptions.merge(getCapabilities(operaDriverProperties));
        return operaOptions;
    }


    static MutableCapabilities getCapabilities(Config config) {
        MutableCapabilities mutableCapabilities = new MutableCapabilities();
        if (Config.getBooleanSystemProperty(PROXY_VARIABLE_NAME)) {
            mutableCapabilities.setCapability(CapabilityType.PROXY, Context.getInstance().getBean(ProxyHandler.class).getJsonProxy());
        }
        if (!config.isEmpty()) {
            String version = String.valueOf(config.getProperty("version", ""));
            Map<String, Object> capabilities = config.getProperty("capabilities", false);
            if (version != null && !version.isEmpty()) {
                mutableCapabilities.setCapability(CapabilityType.BROWSER_VERSION, version);
            }
//          Setting capabilities
            if (capabilities != null && !capabilities.isEmpty()) {
                capabilities.forEach(mutableCapabilities::setCapability);
            }
        }
        return mutableCapabilities;
    }

    private static List<File> getExtensions(List<String> extensions) {
        List<File> files = new ArrayList<>();
        if (extensions != null && !extensions.isEmpty()) {
            extensions.forEach(extensionPathStr -> {
                extensionPathStr = extensionPathStr.trim();
                File extensionFile = new File(extensionPathStr);
                if (extensionFile.exists()) {
                    LOG.info("Adding an extension to browser: " + extensionPathStr);
                    files.add(extensionFile);
                } else {
                    LOG.error("Can't find extension with path: " + extensionPathStr);
                }
            });
        }
        return files;
    }

}
