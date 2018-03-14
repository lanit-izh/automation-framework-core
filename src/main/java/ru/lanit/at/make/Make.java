package ru.lanit.at.make;


import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

public class Make {
    private Wait wait;

    public void setWait(Wait wait) {
        this.wait = wait;
    }

    /**
     * Scrolls to and makes click on given {@link WebElement}. After click waits for page and JS loading complete.
     *
     * @param webElement that should be clicked.
     */
    public void clickTo(WebElement webElement) {
        wait.untilElementVisible(webElement);
        Boolean iFrameFlag = (Boolean) ((JavascriptExecutor) wait.getDriver()).executeScript("return(window == top)");
        if (iFrameFlag) {
            ((JavascriptExecutor) wait.getDriver()).executeScript("arguments[0].scrollIntoView(true);window.scrollBy(0, -400);", webElement);
        } else {
            ((JavascriptExecutor) wait.getDriver()).executeScript("arguments[0].scrollIntoView(true);window.scrollBy(0, -400);",webElement);
        }

        webElement.click();
        wait.untilPageLoaded();
        wait.untilJSComplete();
    }

    /**
     * Cleans and sends keys to given {@link WebElement}
     *
     * @param webElement The element of page with any kind of input.
     * @param message    The message that should be send to an element.
     */
    public void sendKeysTo(WebElement webElement, String message) {
        webElement.clear();
        webElement.sendKeys(message);
    }
}
