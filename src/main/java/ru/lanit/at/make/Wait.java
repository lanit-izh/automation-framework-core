package ru.lanit.at.make;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.util.function.Predicate;

public class Wait {
    private static final int PAGE_MIN_WAIT_TIMEOUT_SEC = 30;
    private static final int ELEMENT_WAIT_TIMEOUT_SEC = 20;
    private static final int DEFAULT_TIMEOUT_SEC = 5;
    private static final int CHECK_STATE_PERIOD_MS = 200;


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

    public void sec(double sec) {
        sleep((int) (sec * 1000));

    }

    /**
     * Waits for given {@link WebElement} to be invisible. Timeout = {@value ELEMENT_WAIT_TIMEOUT_SEC} seconds.
     *
     * @param element WebElement that should be visible.
     */
    public void untilElementInvisible(WebElement element) {
        until(element, WebElement::isDisplayed);
    }

    /**
     * Waits for given {@link WebElement} to be visible. Timeout = {@value ELEMENT_WAIT_TIMEOUT_SEC} seconds.
     *
     * @param element WebElement that should be visible.
     */
    public void untilElementVisible(WebElement element) {
        until(element, WebElement::isDisplayed, ELEMENT_WAIT_TIMEOUT_SEC);
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
        for (WebElement webElement : htmlElements) {
            until(webElement, WebElement::isDisplayed, timeout);
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

    public void until(Supplier<Boolean> waitingCondition) {
        until(waitingCondition, DEFAULT_TIMEOUT_SEC);
    }


    public void until(Supplier<Boolean> waitingCondition, int timeout) {
        long endTime = System.currentTimeMillis() + (long) (timeout * 1000);
        while (!waitingCondition.get() && System.currentTimeMillis() < endTime) {
            sleep(CHECK_STATE_PERIOD_MS);
        }
    }

    public void untilOrException(Supplier<Boolean> waitingCondition, String exceptionMessage) {
        untilOrException(waitingCondition, DEFAULT_TIMEOUT_SEC, exceptionMessage);
    }


    public void untilOrException(Supplier<Boolean> waitingCondition, int timeout, String exceptionMessage) {
        long endTime = System.currentTimeMillis() + (long) (timeout * 1000);
        while (!waitingCondition.get() && System.currentTimeMillis() < endTime) {
            sleep(CHECK_STATE_PERIOD_MS);
        }
        if (!waitingCondition.get()) throw new FrameworkRuntimeException(exceptionMessage);

    }

    /**
     * То же, что и {@link #until(Object, Predicate, double)}, но с таймаутом по-умолчанию {@link #DEFAULT_TIMEOUT_SEC}
     */
    public <T> void until(T obj, Predicate<T> predicate) {
        until(obj, predicate, DEFAULT_TIMEOUT_SEC);
    }

    /**
     * <p>Ожидает, пока указанная функция не начнёт возвращать {@code true} (т.е. выполняется пока результат {@code false}), либо конца таймаута. Например, чтобы подождать появления элемента, вызываем:</p><br/>
     * <p><code>until(webElement, element -> element.isDisplayed(), 20)<br/>// ждёт в течение 20 сек, что элемент появится</code></p>
     *
     * @param obj       объект, над которым производится циклическая проверка
     * @param predicate функция, которая выполняет проверку объекта.
     * @param timeout   таймаут проверки в секундах
     */
    public <T> void until(T obj, Predicate<T> predicate, double timeout) {
        long endTime = System.currentTimeMillis() + (long) (timeout * 1000);
        while (!predicate.test(obj) && System.currentTimeMillis() < endTime) {
            sleep(CHECK_STATE_PERIOD_MS);
        }
    }

    /**
     * Аналогично {@link #until(Object, Predicate, double)}, но если ожидание закончится бросит эксепшен
     *
     * @throws FrameworkRuntimeException если ожидание кончится, а условие не выполнится
     */
    public <T> void untilOrException(T obj, Predicate<T> predicate, double timeout, String errorMessage) {
        until(obj, predicate, timeout);
        if (!predicate.test(obj))
            throw new FrameworkRuntimeException(errorMessage + " Время ожидания: " + timeout + " секунд");
    }

    /**
     * То же, что и {@link #untilOrException(Object, Predicate, double, String)}, но с таймаутом по-умолчанию {@link #DEFAULT_TIMEOUT_SEC}
     */
    public <T> void untilOrException(T obj, Predicate<T> predicate, String errorMessage) {
        untilOrException(obj, predicate, DEFAULT_TIMEOUT_SEC, errorMessage);
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
        untilOrException(this, w -> !w.isJSActive(), ELEMENT_WAIT_TIMEOUT_SEC, "JavaScript (jQuery) выполнялся слишком долго");
    }

    /**
     * Waits for {@value PAGE_MIN_WAIT_TIMEOUT_SEC} seconds for current page to finish loading.
     */
    public void untilPageLoaded() {
        log.debug("Ожидаем загрузки страницы... \t");
        untilOrException(this, Wait::isPageLoaded,
                PAGE_MIN_WAIT_TIMEOUT_SEC, "Страница загружалась слишком долго");
    }

    public void setJsExecutor(JSExecutor jsExecutor) {
        this.jsExecutor = jsExecutor;
    }
}

