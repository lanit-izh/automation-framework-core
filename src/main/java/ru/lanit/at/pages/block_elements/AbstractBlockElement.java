package ru.lanit.at.pages.block_elements;

import io.qameta.atlas.webdriver.AtlasWebElement;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import ru.lanit.at.pages.FrameworkBaseWebElement;

public interface AbstractBlockElement<T extends WebElement> extends AtlasWebElement<T>, FrameworkBaseWebElement, WrapsDriver {

    @Override
    default WebElement getWrappedElement(){
        return this;
    }
}
