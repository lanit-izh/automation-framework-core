package ru.lanit.at.make;


import org.openqa.selenium.WebElement;

public class Make {
    private Wait wait;

    public void setWait(Wait wait) {
        this.wait = wait;
    }

    public void clickTo(WebElement elem) {
        wait.untilJSComplete();
        elem.click();
        wait.untilPageLoaded();
        wait.untilJSComplete();
    }

    public void keysSendTo(WebElement webElement, String message) {
        webElement.clear();
        webElement.sendKeys(message);
    }
}
