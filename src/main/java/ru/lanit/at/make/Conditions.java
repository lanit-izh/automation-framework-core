package ru.lanit.at.make;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.function.Predicate;

public class Conditions {
    public static final Predicate<WebElement> notAnimating = element -> {
        try {
            Point initialLocation = element.getLocation();
            String opacity = element.getCssValue("opacity");
            Dimension initialSize = element.getSize();
            Thread.sleep(50);
            return initialLocation.equals(element.getLocation())
                    && opacity.equals(element.getCssValue("opacity"))
                    && initialSize.equals(element.getSize());
        } catch (InterruptedException | WebDriverException | NullPointerException ignore) {
            return true;
        }
    };
}
