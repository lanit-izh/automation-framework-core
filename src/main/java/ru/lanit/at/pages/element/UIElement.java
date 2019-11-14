package ru.lanit.at.pages.element;

import io.qameta.atlas.webdriver.AtlasWebElement;
import ru.lanit.at.context.Context;
import ru.lanit.at.make.Make;

public interface UIElement extends AtlasWebElement {
    default Make make() {
        return Context.getInstance().getBean(Make.class);
    }
}
