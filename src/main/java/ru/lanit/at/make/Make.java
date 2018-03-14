package ru.lanit.at.make;


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import ru.lanit.at.driver.DriverManager;

public class Make {

    private Wait wait;

    private DriverManager driverManager;

    private JSExecutor jsExecutor;

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
     * Scrolls to and makes click on given {@link WebElement}. After click waits for page and JS loading complete.
     *
     * @param webElement that should be clicked.
     */
    public void clickTo(WebElement webElement) {
        wait.untilElementVisible(webElement);
        Boolean iFrameFlag = (Boolean) jsExecutor.executeScript("return(window == top)");
        if (iFrameFlag) {
            scrollIntoView(webElement);
        } else {
            jsExecutor.executeScript("window.scrollTo(0," + (webElement.getLocation().getY() - getDriver().manage().window().getSize().height / 3) + ")");
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

    public void jsClickOn(WebElement webElement){
        scrollIntoView(webElement);
        jsExecutor.executeScript("arguments[0].click();", webElement);
    }

    public WebDriver getDriver() {
        return driverManager.getDriver();
    }


    private void scrollIntoView(WebElement webElement){
        jsExecutor.executeScript(
                "arguments[0].scrollIntoView({block: 'center'});",
                webElement);
    }
}
