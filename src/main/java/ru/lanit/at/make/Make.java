package ru.lanit.at.make;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import ru.lanit.at.driver.DriverManager;
import ru.yandex.qatools.htmlelements.element.HtmlElement;
import ru.yandex.qatools.htmlelements.element.Named;

public class Make {

    private Wait wait;

    private DriverManager driverManager;

    private JSExecutor jsExecutor;

    private Logger log = LogManager.getLogger(Make.class);

    public void setWait(Wait wait) {
        this.wait = wait;
    }

    public void setJsExecutor(JSExecutor jsExecutor) {
        this.jsExecutor = jsExecutor;
    }

    public void setDriverManager(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    /**
     * Scrolls to and makes click on provided {@link WebElement}. After click waits for page and JS loading complete.
     *
     * @param webElement that should be clicked.
     */
    public void clickTo(WebElement webElement) {
        logAction(webElement, "Click on {}");
        wait.untilElementVisible(webElement);
        Boolean iFrameFlag = (Boolean) jsExecutor.executeScript("return(window == top)");
        if (iFrameFlag) {
            scrollIntoView(webElement);
        } else {
            jsExecutor.executeScript("arguments[0].scrollIntoView(true);window.scrollBy(0, -400);", webElement);
        }

        webElement.click();
        wait.untilPageLoaded();
        wait.untilJSComplete();
    }

    /**
     * Cleans and sends keys to provided {@link WebElement}
     *
     * @param webElement The element of page with any kind of input.
     * @param message    The message that should be send to an element.
     */
    public void sendKeysTo(WebElement webElement, String message) {
        logAction(webElement, "Sending keys '" + message + "' to {}");
        webElement.click();
        webElement.clear();
        webElement.sendKeys(message);
    }

    public void jsClickOn(WebElement webElement) {
        logAction(webElement, "Calling JavaScript click on {}");
        scrollIntoView(webElement);
        jsExecutor.executeScript("arguments[0].click();", webElement);
    }

    private void logAction(WebElement webElement, String message, String... args) {
        int stringCutLength = 80;

        if (webElement instanceof Named) {
            String elementText = null;
            try{
                elementText = webElement.getText();
            } catch (NoSuchElementException ignore){}

            if(elementText != null && !elementText.isEmpty()){
                if(elementText.length() > stringCutLength) elementText = elementText.substring(0, stringCutLength) + "...";
                message += " (" + elementText + ")";
            }

            log.info(message, ((Named) webElement).getName(), args);
        }
    }

    /**
     * Emulates mouse focus on element.
     *
     * @param webElement pure WebElement.
     */
    public void focusOnElement(WebElement webElement) {
        logAction(webElement, "Focus on {}");
        try {
            scrollIntoView(webElement);
            new Actions(getDriver()).moveToElement(webElement).perform();
        } catch (Exception ignore) {
        }
    }

    public void focusOnElement(HtmlElement htmlElement) {
        logAction(htmlElement, "Focus on {}");
        focusOnElement(htmlElement.getWrappedElement());
    }

    public WebDriver getDriver() {
        return driverManager.getDriver();
    }

    public void scrollIntoView(WebElement webElement) {
        jsExecutor.executeScript(
                "arguments[0].scrollIntoView(true);window.scrollBy(0, -400);",
                webElement);
    }
}
