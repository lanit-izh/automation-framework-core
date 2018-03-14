package ru.lanit.at.make;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.util.Date;

public class Wait {

    private static final int CHECK_PAGE_STATE_PERIOD_MS = 300;
    private static final int PAGE_MIN_WAIT_TIMEOUT_SEC = 30;
    private static final int ELEMENT_WAIT_TIMEOUT_SEC = 20;
    private static final int PAGE_WAIT_TIMEOUT_SEC = 60;
    private static final int DEFAULT_TIMEOUT_SEC = 5;
    private static final int CHECK_JS_STATE_PERIOD_MS = 200;
    private Logger log = LogManager.getLogger(Wait.class.getSimpleName());

    private DriverManager driverManager;
    private JSExecutor jsExecutor;

    private void sleep(int ms) {
        try {
            log.trace("Ждём {} миллисекунд...", ms);
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Simple wrapper for {@link Thread#sleep(long)}.
     *
     * @param sec Time to sleep in seconds.
     * @deprecated Use only in extreme need. Replacement methods: {@link #untilJSComplete()}, {@link #untilPageLoaded()}, {@link #untilElementVisible(WebElement)}, {@link #untilElementClickable(WebElement...)}, {@link #untilElementVisible(WebElement)}.
     */
    @Deprecated
    public void sec(double sec) {
        sleep((int) (sec * 1000));
    }

    /**
     * Waits for given {@link WebElement} to be invisible. Timeout = {@value ELEMENT_WAIT_TIMEOUT_SEC} seconds.
     *
     * @param element WebElement that should be visible.
     */
    public void untilElementInvisible(WebElement element) {
        Timeout time = new Timeout(ELEMENT_WAIT_TIMEOUT_SEC);
        try {
            while (element.isDisplayed() && time.notOver()) {
                sleep(CHECK_PAGE_STATE_PERIOD_MS);
            }
        } catch (NoSuchElementException ignore) {
        }
    }

    /**
     * Waits for given {@link WebElement} to be visible. Timeout = {@value ELEMENT_WAIT_TIMEOUT_SEC} seconds.
     *
     * @param element WebElement that should be visible.
     */
    public void untilElementVisible(WebElement element) {
        boolean elementVisible = false;
        Timeout time = new Timeout(ELEMENT_WAIT_TIMEOUT_SEC);
        while (!elementVisible && time.notOver()) {
            try {
                elementVisible = element.isDisplayed();
            } catch (NoSuchElementException nse) {
                sleep(CHECK_PAGE_STATE_PERIOD_MS);
            }
        }
    }

    /**
     * @return Instance of {@link WebDriver} which implements current waiters.
     */
    public WebDriver getDriver() {
        return driverManager.getDriver();
    }

    public void setDriverManager(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    /**
     * Simple waiter. Uses {@link WebDriverWait} and {@link ExpectedConditions}. Waits that all elements of given list will be clickable.
     *
     * @param timeout      Time to wait in seconds.
     * @param htmlElements List of elements that should be clickable.
     */
    public void untilElementClickable(int timeout, WebElement... htmlElements) {
        WebDriverWait wait = new WebDriverWait(driverManager.getDriver(), timeout);
        for (WebElement webElement : htmlElements) {
            wait.until(ExpectedConditions.elementToBeClickable(webElement));
        }
    }

    /**
     * Simple waiter. Uses {@link WebDriverWait} and {@link ExpectedConditions}. Waits that all elements of given list will be clickable.
     *
     * @param htmlElements List of elements that should be clickable.
     */
    public void untilElementClickable(WebElement... htmlElements) {
        untilElementClickable(DEFAULT_TIMEOUT_SEC, htmlElements);
    }

    /**
     * Determines if JavaScript is active on currently selected browser window or frame.
     *
     * @return true if jQuery.active = 0.
     */
    public boolean isJSActive() {
        try {
            return !jsExecutor.executeScript("return jQuery.active").toString().equals("0");
        } catch (Exception ignore) {
            return false;
        }
    }

    /**
     * Determines if page is loaded completely in currently selected browser window or frame.
     *
     * @return true if document.readyState = complete.
     */
    public boolean isPageLoaded() {
        return jsExecutor.executeScript("return document.readyState").equals("complete");
    }

    // страница загрузилась достаточно, чтобы с ней можно было взаимодействовать
    public boolean isPageInteractive() {
        return isPageLoaded() || jsExecutor.executeScript("return document.readyState").equals("interactive");
    }

    /**
     * Waits for {@value ELEMENT_WAIT_TIMEOUT_SEC} seconds for JavaScript to finish scripts execution.
     */
    public void untilJSComplete() {
        Timeout time = new Timeout(ELEMENT_WAIT_TIMEOUT_SEC);
        while (isJSActive()) {

            if (time.over())
                throw new FrameworkRuntimeException("Анимация происходила слишком долго, " + ELEMENT_WAIT_TIMEOUT_SEC + " сек");

            sleep(CHECK_JS_STATE_PERIOD_MS);
        }
    }

    /**
     * Waits for {@value PAGE_WAIT_TIMEOUT_SEC} seconds for current page to finish loading.
     */
    public void untilPageLoaded() {
        Timeout waitFullLoad = new Timeout(PAGE_WAIT_TIMEOUT_SEC);
        Timeout waitMinLoad = new Timeout(PAGE_MIN_WAIT_TIMEOUT_SEC);
        log.debug("Ожидаем загрузки страницы... \t");

        while (!isPageLoaded()) {
            sleep(CHECK_PAGE_STATE_PERIOD_MS);

            if (waitMinLoad.over() && isPageInteractive()) {
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

    public void setJsExecutor(JSExecutor jsExecutor) {
        this.jsExecutor = jsExecutor;
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

