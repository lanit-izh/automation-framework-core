package ru.lanit.at.make;


import org.openqa.selenium.WebElement;

public class Make {
    private Wait wait;

    public void setWait(Wait wait) {
        this.wait = wait;
    }

    public void clickTo(WebElement elem) {
        wait.untilElementVisible(elem);
        elem.click();
        wait.untilPageLoaded();
        wait.untilJSComplete();
    }

    public void sendKeysTo(WebElement webElement, String message) {
        webElement.clear();
        webElement.sendKeys(message);
    }
}
