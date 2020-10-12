package ru.lanit.at.pages;

import io.appium.java_client.AppiumDriver;
import io.qameta.atlas.appium.Screen;


import ru.lanit.at.context.Context;
import ru.lanit.at.driver.DriverManager;


public interface AbstractScreen extends Screen, AbstractPage {
    @Override
    default boolean isOpen() {
        throw new UnsupportedOperationException("This method unsupported in " + this.getClass());
    }

    @Override
    default void open(String url) {
        throw new UnsupportedOperationException("This method unsupported in " + this.getClass());
    }

    @Override
    default void open() {
        throw new UnsupportedOperationException("This method unsupported in " + this.getClass());

    }

    @Override
    default AppiumDriver getWrappedDriver() {
        return (AppiumDriver) Context.getInstance().getBean(DriverManager.class).getDriver();
    }
}
