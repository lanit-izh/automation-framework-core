package ru.lanit.at.steps;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.exceptions.FrameworkRuntimeException;
import ru.lanit.at.pages.AbstractPage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

abstract class AbstractSteps {

    protected Logger log = Logger.getLogger(this.getClass());
    private WebDriver driver;

    protected WebDriver getDriver() {
        if (driver == null) {
            driver = DriverManager.getDriver();
        }
        return driver;
    }

    protected void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    /**
     * Initializes instance of PageObject and updates currentPage.
     * @param clazz descendant of AbstractPage to initialize.
     * @return Instance of clazz
     */
    protected <T extends AbstractPage> T initPage(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getConstructor(WebDriver.class);
            T page = constructor.newInstance(getDriver());
            AbstractPage.setCurrentPage(page);
            log.info("Установлена страница " + page.getClass().getSimpleName());
            return page;
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new FrameworkRuntimeException(e);
        }
    }

    protected <T extends AbstractPage> T openPage(Class<T> clazz) {

        System.out.println("--openPage (" + clazz + ")");
        if (AbstractPage.currentPage.get() != null
                && AbstractPage.currentPage.get().getClass() == clazz) return (T) AbstractPage.currentPage.get();
        try {
            log.info("Переходим на страницу " + clazz.getSimpleName());
            return openPageFromCurrentPage(clazz);
        } catch (RuntimeException ignore) {
            log.warn("Относительный переход не удался. Идём через главную страницу.");
            return openPageByFullPath(clazz);
        }
    }

    private <T extends AbstractPage> T openPageFromCurrentPage(Class<T> clazz) {
        if (AbstractPage.currentPage.get() != null) {
            for (Method method : AbstractPage.currentPage.get().getClass().getDeclaredMethods()) {
                if (clazz.equals(method.getGenericReturnType())
                        && method.getName().matches("(?i)open.+page")) {
                    try {
                        method.setAccessible(true);
                        log.info("Выполняем " + method.getName() + " из " + clazz.getSimpleName());
                        return (T) method.invoke(AbstractPage.currentPage.get());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        throw new RuntimeException();
    }

    protected abstract <T extends AbstractPage> T openPageByFullPath(Class<T> clazz);

}
