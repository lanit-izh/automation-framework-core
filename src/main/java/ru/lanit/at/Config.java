package ru.lanit.at;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;


public class Config {
    private static final Logger LOGGER = LogManager.getLogger(Config.class);

    private static final String DEBUG_PROPERTIES = "/default.properties";


    public static String loadProperty(String name) {
        return loadProperty(name, System.getProperty("application.properties", DEBUG_PROPERTIES));
    }

    public static String loadProperty(String name, String fromResource) {
        Properties props = new Properties();
        try {
            if(!fromResource.startsWith("/")) fromResource="/"+fromResource;
            props.load(Config.class.getResourceAsStream(fromResource));
        } catch (IOException e) {
            throw new FrameworkRuntimeException("Ошибка получение значение'" + name + "' из конфиг файла'" + fromResource + "'.", e);
        }
        String value = props.getProperty(name);
        if (value == null) {
            return null;
        }
        if (value.trim().equalsIgnoreCase("${" + name + "}")) {
            return null;
        }
        return value;
    }


    public static String getStringSystemProperty(String variableName, String defaultValue) {

        String variable = System.getProperty(variableName);
        if (variable != null) {
            return variable.trim();
        }
        variable = loadProperty(variableName);
        if (variable == null || variable.isEmpty()) {
            LOGGER.warn("Не установлено значение параметра: '" + variableName + "', вместо него будет установлено дефолтное значение :'" + defaultValue + "'.");
            return defaultValue;
        }
        return variable.trim();
    }

    /**
     * Tries to read system variable. By default returns false.
     *
     * @param variableName name of system variable.
     * @return {@code false} by default. True if system variable is set and {@code = true}
     */
    public static boolean getBooleanSystemProperty(String variableName) {
        String variable = System.getProperty(variableName);
        if(variable==null){
            variable = loadProperty(variableName);
        }

        return variable != null && !variable.isEmpty() && Boolean.parseBoolean(variable.trim());
    }


    private final Map<String, Object> propertyMap;

    public String getConfigName() {
        return configName;
    }

    private String configName;

    public Config(String configName) {
        this.configName = configName;
        propertyMap = readProperties(configName);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readProperties(String configName) {
        InputStream input = getClass().getClassLoader().getResourceAsStream(configName);
        if (input == null) {
            LOGGER.warn("No :'" + configName + "', config file detected." +
                    " It's strongly recommended to create file '" + configName + "' with  configuration in 'source' directory of your project.");
            return null;
        }
        Yaml yaml = new Yaml();
        return (Map<String, Object>) yaml.load(input);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String propertyName, boolean notNull) {
        T property = null;
        if (!isEmpty())
            property = (T) propertyMap.get(propertyName);
        if (notNull && property == null)
            throw new FrameworkRuntimeException(propertyName + " is null or missing in the config '" + configName + "'");
        return property;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String propertyName, T defaultValue) {
        if (isEmpty()) return defaultValue;
        T property = (T) propertyMap.get(propertyName);
        if (defaultValue != null && property == null) property = defaultValue;
        return property;
    }

    public boolean isEmpty() {
        return propertyMap == null || propertyMap.isEmpty();
    }
}
