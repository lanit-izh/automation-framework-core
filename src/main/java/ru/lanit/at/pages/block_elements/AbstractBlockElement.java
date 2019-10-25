package ru.lanit.at.pages.block_elements;

import io.qameta.atlas.webdriver.AtlasWebElement;
import ru.lanit.at.pages.annotations.Block;
import ru.lanit.at.utils.SearchBlockElement;

//
//public interface AbstractBlockElement<T extends WebElement> extends AtlasWebElement<T>, WrapsDriver , SearchBlockElement {
@Block()
public interface AbstractBlockElement extends AtlasWebElement, SearchBlockElement {
//
//    default String getBlockName() {
//        Title elementName = this.getClass().getAnnotation(Title.class);
//        if (elementName == null)
//            throw new FrameworkRuntimeException(this.getClass().getSimpleName() + " block name is empty. Please ensure that @Title presents before calling getBlockName method.");
//        return elementName.values();
//    }


}
