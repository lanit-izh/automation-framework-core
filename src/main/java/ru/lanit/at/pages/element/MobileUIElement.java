package ru.lanit.at.pages.element;

import io.qameta.atlas.appium.AtlasMobileElement;
import io.qameta.atlas.webdriver.extension.FindBy;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebElement;


import java.util.HashMap;

public interface MobileUIElement extends UIElement, AtlasMobileElement {


    default String getXpath(){
        return this.make().getClass().getAnnotation(FindBy.class).value();
    }


}
