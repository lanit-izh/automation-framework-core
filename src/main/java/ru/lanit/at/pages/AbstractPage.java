package ru.lanit.at.pages;

import io.qameta.atlas.webdriver.WebPage;
import org.openqa.selenium.WebDriver;
import ru.lanit.at.context.Context;
import ru.lanit.at.driver.DriverManager;

public interface AbstractPage extends WebPage, SearchBlockElement {

    /**
     * this method must be implemented each page individually.
     *
     * @return <code>true</code> if has evidence that exactly desirable page is open or <code>false</code> if not
     * @since 4.0.10
     */
    default boolean isOpen() {
        System.err.println("You must override method 'boolean isOpen()' in " + this.getClass().getInterfaces()[0] + " for correct checking opened page!");
        return false;
    }

    @Override
    default WebDriver getWrappedDriver() {
        return Context.getInstance().getBean(DriverManager.class).getDriver();
    }
}
