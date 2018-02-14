package ru.lanit.at.steps;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import ru.lanit.at.context.Context;
import ru.lanit.at.pages.AbstractPage;
import ru.lanit.at.pages.PageCatalog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AbstractFrameworkSteps {

    protected Logger log = LogManager.getLogger(this.getClass());
    private PageCatalog pageCatalog;
    private WebDriver driver;

    public AbstractFrameworkSteps() {
        pageCatalog = (PageCatalog) Context.getInstance().getBean("pageCatalog");
    }

    protected WebDriver getDriver() {
        if (driver == null) {
            driver = (WebDriver) Context.getInstance().getBean("webDriver");
        }
        return driver;
    }

    protected void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    protected <T extends AbstractPage> T getPage(Class<T> clazz) {
        return pageCatalog.getPage(clazz);
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

    @Deprecated
    protected <T extends AbstractPage> T initPage(Class<T> clazz){
        return getPage(clazz);
    }
}
