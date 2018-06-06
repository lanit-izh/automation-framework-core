package ru.lanit.at.pages;

import org.openqa.selenium.WebDriver;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.exceptions.FrameworkRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Catalog that initializes, saves and provides PageObjects which are required during the test run.
 */
public class PageCatalog {
    private List<AbstractPage> pageList = new LinkedList<>();
    private DriverManager driverManager;
    private WebDriver previousDriver;
    /**
     * Variable for tracking which page object is currently opened and in use.
     */
    private AbstractPage currentPage;

    /**
     * Returns instance of requested page object class from catalog. If catalog doesn't have such instance - initializes it and saves in catalog.
     *
     * @param clazz Class of page object to find or initialize in catalog.
     * @return Instance of clazz.
     */
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
                Constructor<T> constructor = clazz.getConstructor();
                T page = constructor.newInstance();
                setCurrentPage(page);
                pageList.add(page);
                return page;
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new FrameworkRuntimeException(e);
            }
        }
    }

    /**
     * Returns instance of class if catalog contains such instance or null if it doesn't.
     *
     * @param clazz Class of page object that should be found in catalog.
     * @return Instance of clazz from catalog or null if catalog doesn't have any instance of clazz.
     */
    @SuppressWarnings("unchecked")
    private <T extends AbstractPage> T getPageFromList(Class<T> clazz) {
        ListIterator<AbstractPage> litr = pageList.listIterator(pageList.size());
        while (litr.hasPrevious()) {
            AbstractPage abstractPage = litr.previous();
            if (clazz.isAssignableFrom(abstractPage.getClass())) return (T) abstractPage;
        }
        return null;
    }

    /**
     * @return Page object that is currently opened.
     */
    public AbstractPage getCurrentPage() {
        return currentPage;
    }

    /**
     * Setup current page.
     *
     * @param abstractPage Page object that should be set as current page.
     */
    public void setCurrentPage(AbstractPage abstractPage) {
        if (currentPage == null || currentPage != abstractPage) {
            currentPage = abstractPage;
        }
    }

    public WebDriver getDriver() {
        return driverManager.getDriver();
    }

    public void setDriverManager(DriverManager driverManager) {
        this.driverManager = driverManager;
    }
}
