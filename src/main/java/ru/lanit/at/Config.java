package ru.lanit.at;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.io.InputStream;
import java.util.Map;

public class Config {

    public static String getStringSystemProperty(String variableName, String defaultValue) {
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
    public static boolean getBooleanSystemProperty(String variableName) {
        String variable = System.getProperty(variableName);
        return variable != null && !variable.isEmpty() && Boolean.parseBoolean(variable.trim());
    }

    private final Logger log = LogManager.getLogger(this.getClass());
    private final Map<String, Object> propertyMap;
    private String configName;

    public Config(String configName) {
        this.configName = configName;
        propertyMap = readProperties(configName);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readProperties(String configName) {
        InputStream input = getClass().getClassLoader().getResourceAsStream(configName);
        if (input == null) {
            log.warn("No " + configName + " config file detected." +
                    " It's strongly recommended to create file '" + configName + "' with driver configuration in 'source' directory of your project." +
                    " Creating driver with default properties.");
            return null;
        }
        Yaml yaml = new Yaml();
        return (Map<String, Object>) yaml.load(input);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String propertyName, boolean notNull){
        T property = (T) propertyMap.get(propertyName);
        if(notNull && property == null) throw new FrameworkRuntimeException(propertyName + " is null or missing in the config '" + configName + "'");
        return property;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String propertyName, T defaultValue){
        T property = (T) propertyMap.get(propertyName);
        if(defaultValue != null && property == null) property = defaultValue;
        return property;
    }

    public boolean isEmpty() {
        return propertyMap == null || propertyMap.isEmpty();
    }
}
