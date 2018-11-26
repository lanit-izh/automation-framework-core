package ru.lanit.at.pages.block_elements;

import io.qameta.atlas.AtlasWebElement;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import ru.lanit.at.pages.FrameworkElement;

public interface AbstractBlockElement<T extends WebElement> extends AtlasWebElement<T>, FrameworkElement, WrapsDriver {}
