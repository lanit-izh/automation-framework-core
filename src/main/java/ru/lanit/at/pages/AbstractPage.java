package ru.lanit.at.pages;

import io.qameta.atlas.webdriver.WebPage;
import org.openqa.selenium.WebDriver;
import ru.lanit.at.context.Context;
import ru.lanit.at.driver.DriverManager;

public interface AbstractPage extends WebPage, SearchBlockElement {

    @Override
    default WebDriver getWrappedDriver() {
        return Context.getInstance().getBean(DriverManager.class).getDriver();
    }
}
