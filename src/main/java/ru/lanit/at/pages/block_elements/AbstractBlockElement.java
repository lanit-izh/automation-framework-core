package ru.lanit.at.pages.block_elements;

import ru.lanit.at.context.Context;
import ru.lanit.at.make.Make;
import ru.lanit.at.make.Wait;
import ru.yandex.qatools.htmlelements.element.HtmlElement;

public class AbstractBlockElement extends HtmlElement {

    protected Wait wait;
    protected Make make;

    public AbstractBlockElement() {
        wait = (Wait) Context.getInstance().getBean("wait");
        make = (Make) Context.getInstance().getBean("make");
    }
}
