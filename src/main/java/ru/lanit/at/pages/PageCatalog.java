package ru.lanit.at.pages;

import org.openqa.selenium.WebDriver;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public class PageCatalog {
    private Set<AbstractPage> pageSet = new HashSet<>();
    private WebDriver driver;

    public <T extends AbstractPage> T getPage(Class<T> clazz) {
        if (setContains(clazz)) return getPageFromSet(clazz);
        else {
            try {
                Constructor<T> constructor = clazz.getConstructor(WebDriver.class);
                T page = constructor.newInstance(driver);
                AbstractPage.setCurrentPage(page);
                pageSet.add(page);
                return page;
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new FrameworkRuntimeException(e);
            }
        }
    }

    private <T extends AbstractPage> T getPageFromSet(Class<T> clazz) {
        for (AbstractPage abstractPage : pageSet) {
            if (abstractPage.getClass() == clazz) return (T) abstractPage;
        }
        throw new FrameworkRuntimeException("There is no " + clazz.getSimpleName() + " in page catalog.");
    }

    private <T extends AbstractPage> boolean setContains(Class<T> clazz) {
        for (AbstractPage abstractPage : pageSet) {
            if (abstractPage.getClass() == clazz) return true;
        }
        return false;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }
}
