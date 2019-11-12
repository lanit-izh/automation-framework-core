package ru.lanit.at.pages.block;

import io.qameta.atlas.webdriver.AtlasWebElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.lanit.at.context.Context;
import ru.lanit.at.make.Make;
import ru.lanit.at.utils.SearchBlockElement;


public interface AbstractBlockElement extends AtlasWebElement, SearchBlockElement {

    default Logger log() {
        return LogManager.getLogger(getClass());
    }

    default Make make() {
        return Context.getInstance().getBean(Make.class);
    }
}
