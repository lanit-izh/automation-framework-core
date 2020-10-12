package ru.lanit.at.steps;

import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import ru.lanit.at.Config;
import ru.lanit.at.pages.element.MobileUIElement;
import ru.lanit.at.pages.element.UIElement;

import static io.appium.java_client.touch.offset.PointOption.point;
import static ru.lanit.at.FrameworkConstants.*;


public class AbstractFrameworkAndroidSteps extends AbstractFrameworkSteps {

    static private final int numberOfScrolls;
    static {
        numberOfScrolls =  new Config(DEFAULT_TIMEOUTS_CONFIG).getProperty(SCROLL_TIMES, 10);
    }

    public int getNumberOfScrolls(){
        return numberOfScrolls;

    }
    public MobileUIElement scrollToElement(Class<? extends UIElement> type, int numberOfTimes, String... params) {
        MobileUIElement element = null;

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
        return element;


    }

    public MobileUIElement scrollToElement(Class<? extends UIElement> type, String... params) {
        return scrollToElement(type, numberOfScrolls, params);

    }

    public MobileUIElement scrollToElementByName(Class<? extends UIElement> type, String elementName, int numberOfTimes) {
        MobileUIElement element = null;

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
        return element;


    }

    public MobileUIElement scrollToElementByName(Class<? extends UIElement> type, String elementName) {
        return scrollToElementByName(type, elementName, numberOfScrolls);

    }


    public void scroll(boolean isDown) {

        int anchor;
        int startPoint;
        int endPoint;
        Dimension size = getDriver().manage().window().getSize();
        if (isDown) {
            anchor = (int) (size.width / 2);
            startPoint = (int) (size.height / 2);
            endPoint = size.height / 8;
        } else {
            anchor = (int) (size.width / 2);
            startPoint = size.height - (size.height / 8);
            endPoint = (int) (size.height / 2);
        }
        new TouchAction((PerformsTouchActions) getDriver())
                .longPress(point(anchor, startPoint))
                .moveTo(point(anchor, endPoint))
                .release()
                .perform();
    }
}
