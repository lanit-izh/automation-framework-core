package ru.lanit.at.pages.block_elements;

import io.qameta.atlas.webdriver.AtlasWebElement;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import ru.lanit.at.pages.FrameworkBaseElement;

public interface AbstractBlockElement<T extends WebElement> extends AtlasWebElement<T>, FrameworkBaseElement, WrapsDriver {

    @Override
    default WebElement getWrappedElement(){
        return this;
    }
}
