package ru.lanit.at.make;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

public class Wait {

    private final int DEFAULT_TIMEOUT = 5;
    private final int CHECK_JS_STATE_PERIOD = 200;
    private static final int CHECK_PAGE_STATE_PERIOD = 300;

    private Logger log = LogManager.getLogger(Wait.class.getSimpleName());

    private WebDriver driver;

    private void sleep(int msec) {
        try {
            log.trace("Ждём {} миллисекунд...", msec);
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public void untilElementClickable(int timeout, WebElement... htmlElements) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        for (WebElement webElement : htmlElements) {
            wait.until(ExpectedConditions.elementToBeClickable(webElement));
        }
    }

    public void untilElementClickable(WebElement... htmlElements) {
        untilElementClickable(DEFAULT_TIMEOUT, htmlElements);
    }

    private JavascriptExecutor getJSExecutor() {
        if (driver != null) {
            return (JavascriptExecutor) driver;
        } else {
            throw new FrameworkRuntimeException("Драйвер не запущен! Сначала инициализируйте драйвер");
        }
    }

    private String executeScript(String jsCommand, Object... args) {
        Object o = getJSExecutor().executeScript(jsCommand, args);
        return o != null ? o.toString() : "";
    }

    public boolean isJSActive() {
        return !executeScript("return jQuery.active").equals("0");
    }

    public boolean isPageLoaded() {
        return executeScript("return document.readyState").equals("complete");
    }

    // страница загрузилась достаточно, чтобы с ней можно было взаимодействовать
    public boolean isPageInterable() {
        return isPageLoaded() || executeScript("return document.readyState").equals("interactive");
    }

    public void untilJSComplete() {
        int timer = 0;
        while (isJSActive()) {
            sleep(CHECK_JS_STATE_PERIOD);
            if (++timer > 100) {
                log.warn("Анимация происходила слишком долго, " + timer * CHECK_JS_STATE_PERIOD / 1000 + " сек");
                break;
            }
        }
    }

    public void untilPageLoaded() {
        int timer = 0;
        log.debug("Ожидаем загрузки страницы... \t");
        while (!isPageLoaded()) {
            sleep(CHECK_PAGE_STATE_PERIOD);

            if (++timer > 20 && isPageInterable()) {
                log.warn("Страница не была полностью загружена, превышено время ожидания. ({} сек)", CHECK_PAGE_STATE_PERIOD * timer / 1000);
                break;
            }

            if (timer > 100) {
                String errorMessage = "Не удалось загрузить страницу в течение " + CHECK_PAGE_STATE_PERIOD * timer / 1000 + " сек";
                log.error(errorMessage);
                Assert.fail(errorMessage);
                break;
            }
        }
    }

}
