package ru.lanit.at.pages;

import org.openqa.selenium.WebDriver;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class PageCatalog {
    private List<AbstractPage> pageList = new LinkedList<>();
    private DriverManager driverManager;
    private WebDriver previousDriver;
    /**
     * Variable for tracking which page object is currently opened and in use.
     */
    private AbstractPage currentPage;

    public <T extends AbstractPage> T getPage(Class<T> clazz) {

        WebDriver actualDriver = driverManager.getDriver();
        if (previousDriver != actualDriver) {
            previousDriver = actualDriver;
            pageList.clear();
        }

        T requestedPage = getPageFromList(clazz);

        if (requestedPage != null) {
            setCurrentPage(requestedPage);
            return requestedPage;
        } else {
            try {
                Constructor<T> constructor = clazz.getConstructor(WebDriver.class);
                T page = constructor.newInstance(actualDriver);
                setCurrentPage(page);
                pageList.add(page);
                return page;
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new FrameworkRuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractPage> T getPageFromList(Class<T> clazz) {
        ListIterator<AbstractPage> litr = pageList.listIterator(pageList.size());
        while (litr.hasPrevious()) {
            AbstractPage abstractPage = litr.previous();
            if (clazz.isAssignableFrom(abstractPage.getClass())) return (T) abstractPage;
        }
        return null;
    }

    public void setCurrentPage(AbstractPage abstractPage) {
        if (currentPage == null || currentPage != abstractPage) {
            currentPage = abstractPage;
        }
    }

    public AbstractPage getCurrentPage() {
        return currentPage;
    }

    public WebDriver getDriver() {
        return driverManager.getDriver();
    }

    public void setDriverManager(DriverManager driverManager) {
        this.driverManager = driverManager;
    }
}
