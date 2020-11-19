package ru.lanit.at.steps;

import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import ru.lanit.at.pages.element.MobileUIElement;
import ru.lanit.at.pages.element.UIElement;
import java.util.concurrent.TimeUnit;

import static io.appium.java_client.touch.offset.PointOption.point;
import static ru.lanit.at.FrameworkConstants.*;


public class AbstractFrameworkAndroidSteps extends AbstractFrameworkSteps {


    protected int getNumberOfScrolls() {
        return getDriverManager().getDriverTimeouts().getProperty(SCROLL_TIMES, 10);
    }

    protected int getImplicitlyTimeout() {
        return getDriverManager().getDriverTimeouts().getProperty(IMPLICITLY_WAIT, 10);
    }

    public MobileUIElement scrollToElement(Class<? extends UIElement> type, int numberOfTimes, String... params) {
        MobileUIElement element = null;
        getDriver().manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
        for (int i = 0; i < numberOfTimes; i++) {
            try {
                scroll(true);
                element = getUIElement(type, params);
                element.getLocation();
                i = numberOfTimes;
            } catch (NoSuchElementException ex) {
                System.out.println(String.format("Element not available. Scrolling (%s) times...", i + 1));
            }
        }
        getDriver().manage().timeouts().implicitlyWait(getImplicitlyTimeout(), TimeUnit.SECONDS);
        return element;


    }

    public MobileUIElement scrollToElement(Class<? extends UIElement> type, String... params) {
        return scrollToElement(type, getNumberOfScrolls(), params);

    }

    public MobileUIElement scrollToElementByName(Class<? extends UIElement> type, String elementName, int numberOfTimes) {
        MobileUIElement element = null;

        getDriver().manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
        for (int i = 0; i < numberOfTimes; i++) {
            try {
                scroll(true);
                element = getElementByName(elementName, type);
                element.getLocation();
                i = numberOfTimes;
            } catch (NoSuchElementException ex) {
                System.out.println(String.format("Element not available. Scrolling (%s) times...", i + 1));
            }
        }
        getDriver().manage().timeouts().implicitlyWait(getImplicitlyTimeout(), TimeUnit.SECONDS);
        return element;


    }

    public MobileUIElement scrollToElementByName(Class<? extends UIElement> type, String elementName) {
        return scrollToElementByName(type, elementName, getNumberOfScrolls());

    }


    public MobileUIElement swipeToElement(Class<? extends UIElement> type, int numberOfTimes, String... params) {
        MobileUIElement element = null;
        getDriver().manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
        for (int i = 0; i < numberOfTimes; i++) {
            try {
                swipe(true);
                element = getUIElement(type, params);
                element.getLocation();
                i = numberOfTimes;
            } catch (NoSuchElementException ex) {
                System.out.println(String.format("Element not available. Scrolling (%s) times...", i + 1));
            }
        }
        getDriver().manage().timeouts().implicitlyWait(getImplicitlyTimeout(), TimeUnit.SECONDS);
        return element;


    }

    public MobileUIElement swipeToElement(Class<? extends UIElement> type, String... params) {
        return swipeToElement(type, getNumberOfScrolls(), params);

    }

    public MobileUIElement swipeToElementByName(Class<? extends UIElement> type, String elementName, int numberOfTimes) {
        MobileUIElement element = null;

        getDriver().manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
        for (int i = 0; i < numberOfTimes; i++) {
            try {
                swipe(true);
                element = getElementByName(elementName, type);
                element.getLocation();
                i = numberOfTimes;
            } catch (NoSuchElementException ex) {
                System.out.println(String.format("Element not available. Scrolling (%s) times...", i + 1));
            }
        }
        getDriver().manage().timeouts().implicitlyWait(getImplicitlyTimeout(), TimeUnit.SECONDS);
        return element;


    }

    public MobileUIElement swipeToElementByName(Class<? extends UIElement> type, String elementName) {
        return swipeToElementByName(type, elementName, getNumberOfScrolls());

    }


    public void swipeIntoElement(boolean isRight, WebElement element) {
        int startX;
        int endX;
        int startY;
        Dimension size = element.getSize();
        Point position = element.getLocation();
        if (!isRight) {
            startY = (position.y + size.height / 2);
            startX = (int) (position.x + size.width * 0.90);
            endX = (int) (position.x + size.width * 0.05);
        } else {
            startY = (position.y + size.height / 2);
            startX = (int) (position.x + size.width * 0.05);
            endX = (int) (position.x + size.width * 0.90);
        }
        new TouchAction((PerformsTouchActions) getDriver())
                .press(point(startX, startY))
                .moveTo(point(endX, startY))
                .release()
                .perform();
    }

    public void swipe(boolean isRight) {
        int startX;
        int endX;
        int startY;
        Dimension size = getDriver().manage().window().getSize();
        if (!isRight) {
            startY = (size.height / 2);
            startX = (int) (size.width * 0.90);
            endX = (int) (size.width * 0.05);
        } else {
            startY = (size.height / 2);
            startX = (int) (size.width * 0.05);
            endX = (int) (size.width * 0.90);
        }
        new TouchAction((PerformsTouchActions) getDriver())
                .press(point(startX, startY))
                .moveTo(point(endX, startY))
                .release()
                .perform();
    }


    public void tap(int tapX, int tapY) {
        Dimension size = getDriver().manage().window().getSize();
        new TouchAction((PerformsTouchActions) getDriver())
                .tap(point(tapX, tapY)).perform();

    }


    public void scroll(boolean isDown) {

        int anchor;
        int startPoint;
        int endPoint;
        Dimension size = getDriver().manage().window().getSize();
        if (isDown) {
            anchor = size.width / 2;
            startPoint = size.height / 2;
            endPoint = size.height / 8;
        } else {
            anchor = size.width / 2;
            startPoint = size.height - (size.height / 8);
            endPoint = size.height / 2;
        }
        new TouchAction((PerformsTouchActions) getDriver())
                .longPress(point(anchor, startPoint))
                .moveTo(point(anchor, endPoint))
                .release()
                .perform();
    }

    public void scrollIntoElement(boolean isDown, WebElement element) {

        int anchor;
        int startPoint;
        int endPoint;

        Point position = element.getLocation();
        Dimension size = element.getSize();
        if (isDown) {
            anchor = position.x + size.width / 2;
            startPoint = position.y + size.height / 2;
            endPoint = position.y + size.height / 16;
        } else {
            anchor = position.x + size.width / 2;
            startPoint = position.y + size.height - (size.height / 16);
            endPoint = position.y + size.height / 2;
        }
        new TouchAction((PerformsTouchActions) getDriver())
                .longPress(point(anchor, startPoint))
                .moveTo(point(anchor, endPoint))
                .release()
                .perform();
    }
}
