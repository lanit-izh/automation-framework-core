package ru.lanit.at.make;

import org.openqa.selenium.JavascriptExecutor;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

public class JSExecutor {

    private DriverManager driverManager;

    public JavascriptExecutor getJSExecutor() {
        if (driverManager.getDriver() != null) {
            return (JavascriptExecutor) driverManager.getDriver();
        } else {
            throw new FrameworkRuntimeException("Драйвер не запущен! Сначала инициализируйте драйвер");
        }
    }

    public Object executeScript(String jsCommand, Object... args) {
        return getJSExecutor().executeScript(jsCommand, args);
    }

    public void setDriverManager(DriverManager driverManager) {
        this.driverManager = driverManager;
    }
}
