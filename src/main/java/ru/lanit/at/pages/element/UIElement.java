package ru.lanit.at.pages.element;

import io.qameta.atlas.webdriver.AtlasWebElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.lanit.at.context.Context;
import ru.lanit.at.exceptions.FrameworkRuntimeException;
import ru.lanit.at.make.Make;
import ru.lanit.at.pages.annotations.Element;
import ru.lanit.at.pages.annotations.Title;

//public interface UIElement<T extends WebElement> extends AtlasWebElement<T>
@Element
public interface UIElement extends AtlasWebElement {

    default Logger log() {
        return LogManager.getLogger(getClass());
    }


    /**
     * Method to interract with makers.
     *
     * @return Instance of {@link Make}
     */
    default Make make() {
        return Context.getInstance().getBean(Make.class);
    }


    default String[] getElementName() {
        Title elementName = this.getClass().getAnnotation(Title.class);
        if (elementName == null)
            throw new FrameworkRuntimeException(this.getClass().getSimpleName() + " element name is empty. Please ensure that @Title presents before calling getElementName method.");
        return elementName.values();
    }

}
