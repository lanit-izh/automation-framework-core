package ru.lanit.at.make;


import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

public class Make {
    private Wait wait;

    public void setWait(Wait wait) {
        this.wait = wait;
    }

    public void clickTo(WebElement elem) {
        wait.untilElementVisible(elem);
        Boolean iframeFlag = (Boolean) ((JavascriptExecutor)wait.getDriver()).executeScript("return(window == top)");
        if (iframeFlag) {
            ((JavascriptExecutor)wait.getDriver()).executeScript("arguments[0].scrollIntoView(false);window.scrollBy(0, -"+wait.getDriver().manage().window().getSize().height/3+")", elem);
        } else {
            ((JavascriptExecutor)wait.getDriver()).executeScript("window.scrollTo(0," + (elem.getLocation().getY() - wait.getDriver().manage().window().getSize().height/3) + ")");
        }

        elem.click();
        wait.untilPageLoaded();
        wait.untilJSComplete();
    }

    public void sendKeysTo(WebElement webElement, String message) {
        webElement.clear();
        webElement.sendKeys(message);
    }
}
