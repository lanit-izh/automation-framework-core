package ru.lanit.at.make;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.util.Date;

public class Wait {

    private static final int CHECK_PAGE_STATE_PERIOD_MSEC = 300;
    private static final int PAGE_MIN_WAIT_TIMEOUT_SEC = 30;
    private static final int ELEMENT_WAIT_TIMEOUT_SEC = 20;
    private static final int PAGE_WAIT_TIMEOUT_SEC = 60;
    private static final int DEFAULT_TIMEOUT_SEC = 5;
    private static final int CHECK_JS_STATE_PERIOD_MSEC = 200;
    private Logger log = LogManager.getLogger(Wait.class.getSimpleName());

    private DriverManager driverManager;

    private void sleep(int msec) {
        try {
            log.trace("Ждём {} миллисекунд...", msec);
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public void sec(int sec) {
        sleep(sec * 1000);
    }

    public void untilElementInvisible(WebElement element) {
        Timeout time = new Timeout(ELEMENT_WAIT_TIMEOUT_SEC);
        try {
            while (element.isDisplayed() && time.notOver()) {
                sleep(CHECK_PAGE_STATE_PERIOD_MSEC);
            }
        } catch (NoSuchElementException ignore) {
        }
    }

    public void untilElementVisible(WebElement element) {
        boolean elementVisible = false;
        Timeout time = new Timeout(ELEMENT_WAIT_TIMEOUT_SEC);
        while (!elementVisible && time.notOver()) {
            try {
                elementVisible = element.isDisplayed();
            } catch (NoSuchElementException nse) {
                sleep(CHECK_PAGE_STATE_PERIOD_MSEC);
            }
        }
    }

    public WebDriver getDriver() {
        return driverManager.getDriver();
    }

    public void setDriverManager(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    public void untilElementClickable(int timeout, WebElement... htmlElements) {
        WebDriverWait wait = new WebDriverWait(driverManager.getDriver(), timeout);
        for (WebElement webElement : htmlElements) {
            wait.until(ExpectedConditions.elementToBeClickable(webElement));
        }
    }

    public void untilElementClickable(WebElement... htmlElements) {
        untilElementClickable(DEFAULT_TIMEOUT_SEC, htmlElements);
    }

    private JavascriptExecutor getJSExecutor() {
        if (driverManager.getDriver() != null) {
            return (JavascriptExecutor) driverManager.getDriver();
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
        Timeout time = new Timeout(ELEMENT_WAIT_TIMEOUT_SEC);
        while (isJSActive()) {

            if (time.over())
                throw new FrameworkRuntimeException("Анимация происходила слишком долго, " + ELEMENT_WAIT_TIMEOUT_SEC + " сек");

            sleep(CHECK_JS_STATE_PERIOD_MSEC);
        }
    }

    public void untilPageLoaded() {
        Timeout waitFullLoad = new Timeout(PAGE_WAIT_TIMEOUT_SEC);
        Timeout waitMinLoad = new Timeout(PAGE_MIN_WAIT_TIMEOUT_SEC);
        log.debug("Ожидаем загрузки страницы... \t");

        while (!isPageLoaded()) {
            sleep(CHECK_PAGE_STATE_PERIOD_MSEC);

            if (waitMinLoad.over() && isPageInterable()) {
                log.warn("Страница не была полностью загружена, превышено время ожидания. ({} сек)", PAGE_MIN_WAIT_TIMEOUT_SEC);
                break;
            }

            if (waitFullLoad.over()) {
                String errorMessage = "Не удалось загрузить страницу в течение " + PAGE_WAIT_TIMEOUT_SEC + " сек";
                log.error(errorMessage);
                throw new FrameworkRuntimeException(errorMessage);
            }
        }
    }

    class Timeout {
        private long endTime;

        public Timeout(int timeoutInSec) {
            endTime = new Date().getTime() + timeoutInSec * 1000;
        }

        public boolean notOver() {
            return (new Date().getTime()) < endTime;
        }

        public boolean over() {
            return !notOver();
        }
    }
}

