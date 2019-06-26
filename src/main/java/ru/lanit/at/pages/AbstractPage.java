package ru.lanit.at.pages;

import io.qameta.atlas.webdriver.WebPage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.lanit.at.context.Context;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.make.Make;

import java.util.List;

import static ru.lanit.at.FrameworkConstants.DEFAULT_TIMEOUT;

public interface AbstractPage extends WebPage, FrameworkBaseElement {

    @Override
    default WebDriver getWrappedDriver() {
        return Context.getInstance().getBean(DriverManager.class).getDriver();
    }

    /**
     * Waits for the specified time for {@link WebElement} to be visible, using {@link WebDriverWait} and {@link ExpectedConditions#visibilityOf(WebElement)}.
     *
     * @param htmlElement The element that should be visible.
     * @param timeout     Timeout in seconds.
     * @deprecated {@link io.qameta.atlas.webdriver.AtlasWebElement} resolves all wait methods. Additional waitings are redundant.
     */
    @Deprecated
    default void waitForElementVisible(WebElement htmlElement, int timeout) {
        new WebDriverWait(getWrappedDriver(), timeout).until(ExpectedConditions.visibilityOf(htmlElement));
    }

    /**
     * Waits for default timeout for {@link WebElement} to be visible, using {@link WebDriverWait} and {@link ExpectedConditions#visibilityOf(WebElement)}.
     *
     * @param htmlElement The element that should be visible.
     * @deprecated {@link io.qameta.atlas.webdriver.AtlasWebElement} resolves all wait methods. Additional waitings are redundant.
     */
    @Deprecated
    default void waitForElementVisible(WebElement htmlElement) {
        waitForElementVisible(htmlElement, DEFAULT_TIMEOUT);
    }

    /**
     * Waits for the specified time for {@link WebElement} to be invisible, using {@link WebDriverWait} and {@link ExpectedConditions#invisibilityOf(WebElement)}.
     *
     * @param htmlElement The element that should be invisible.
     * @param timeout     Timeout in seconds.
     * @deprecated {@link io.qameta.atlas.webdriver.AtlasWebElement} resolves all wait methods. Additional waitings are redundant.
     */
    @Deprecated
    default void waitForElementInvisible(WebElement htmlElement, int timeout) {
        new WebDriverWait(getWrappedDriver(), timeout).until(ExpectedConditions.invisibilityOf(htmlElement));
    }

    /**
     * Waits for default timeout for {@link WebElement} to be invisible, using {@link WebDriverWait} and {@link ExpectedConditions#visibilityOf(WebElement)}.
     *
     * @param htmlElement The element that should be invisible.
     * @deprecated {@link io.qameta.atlas.webdriver.AtlasWebElement} resolves all wait methods. Additional waitings are redundant.
     */
    @Deprecated
    default void waitForElementInvisible(WebElement htmlElement) {
        new WebDriverWait(getWrappedDriver(), DEFAULT_TIMEOUT).until(ExpectedConditions.invisibilityOf(htmlElement));
    }

    /**
     * Waits for the specified time for list of {@link WebElement} to be clickable, using {@link WebDriverWait} and {@link ExpectedConditions#elementToBeClickable(WebElement)}.
     *
     * @param timeout      Timeout in seconds.
     * @param htmlElements List of elements that should be clickable.
     * @deprecated {@link io.qameta.atlas.webdriver.AtlasWebElement} resolves all wait methods. Additional waitings are redundant.
     */
    @Deprecated
    default void waitForElementClickable(int timeout, WebElement... htmlElements) {
        WebDriverWait wait = new WebDriverWait(getWrappedDriver(), timeout);
        for (WebElement webElement : htmlElements) {
            wait.until(ExpectedConditions.elementToBeClickable(webElement));
        }
    }

    /**
     * Waits for the default timeout for list of {@link WebElement} to be clickable, using {@link WebDriverWait} and {@link ExpectedConditions#elementToBeClickable(WebElement)}.
     *
     * @param webElements List of elements that should be clickable.
     * @deprecated {@link io.qameta.atlas.webdriver.AtlasWebElement} resolves all wait methods. Additional waitings are redundant.
     */
    @Deprecated
    default void waitForElementClickable(WebElement... webElements) {
        waitForElementClickable(DEFAULT_TIMEOUT, webElements);
    }

    default void waitForJSandJQueryToLoad() {
        WebDriverWait wait = new WebDriverWait(getWrappedDriver(), DEFAULT_TIMEOUT);
        ExpectedCondition<Boolean> jQueryLoad = driver -> {
            try {
                return ((Long) ((JavascriptExecutor) driver).executeScript("return jQuery.active") == 0);
            } catch (Exception e) {
                return true;
            }
        };

        ExpectedCondition<Boolean> jsLoad = driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState")
                .toString().equals("complete");

        wait.until(jQueryLoad);
        wait.until(jsLoad);
    }

    /**
     * Click on element and wait until page loaded completely, including JS & animation finish.
     * @param element that chould be clicked
     * @deprecated {@link io.qameta.atlas.webdriver.AtlasWebElement} resolves all wait methods. Additional waitings are redundant.
     */
    @Deprecated
    default void clickAndWait(WebElement element) {
        waitForElementClickable(element);
        element.click();
        waitForJSandJQueryToLoad();
    }

    /**
     * @return Default timeout in seconds
     */
    default int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    /**
     * Method that tries to find element of page with given text. Searching occurs by xpath = {@code "//*[text()='" + text + "']"}
     *
     * @param text key text that should be found on the page.
     * @return WebElement that contains given text.
     */
    default WebElement getElementByText(String text) {
        String xPath = "//*[text()='" + text + "']";
        List<WebElement> foundElements = getWrappedDriver().findElements(By.xpath(xPath));
        if (foundElements.size() == 1) return foundElements.get(0);
        if (foundElements.size() > 1) {
            log().warn(foundElements.size() + " elements with text '" + text + "' were found on " + this.getClass().getSimpleName()
                    + ". Returning first element of list.");
            return foundElements.get(0);
        }
        throw new NoSuchElementException("No elements with text '" + text + "' were found on " + this.getClass().getSimpleName());
    }
}
