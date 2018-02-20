package ru.lanit.at.pages;

import org.openqa.selenium.WebDriver;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public class PageCatalog {
    private Set<AbstractPage> pageSet = new HashSet<>();
    private DriverManager driverManager;
    private WebDriver previousDriver;

    public PageCatalog(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    public <T extends AbstractPage> T getPage(Class<T> clazz) {

        if(previousDriver != driverManager.getDriver()) pageSet.clear();

        if (setContains(clazz)){
            T requestedPage = getPageFromSet(clazz);
            AbstractPage.setCurrentPage(requestedPage);
            return requestedPage;
        }
        else {
            try {
                Constructor<T> constructor = clazz.getConstructor(WebDriver.class);
                T page = constructor.newInstance(driverManager.getDriver());
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
        return driverManager.getDriver();
    }

    public void setDriverManager(DriverManager driverManager) {
        this.driverManager = driverManager;
    }
}
