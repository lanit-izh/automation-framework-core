package ru.lanit.at.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import ru.lanit.at.assertion.AssertsManager;
import ru.lanit.at.context.Context;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.pages.AbstractPage;
import ru.lanit.at.pages.PageCatalog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public abstract class AbstractFrameworkSteps {

    protected Logger log = LogManager.getLogger(this.getClass());
    protected AssertsManager assertsManager;
    private PageCatalog pageCatalog;

    public AbstractFrameworkSteps() {
        pageCatalog = Context.getInstance().getBean(PageCatalog.class);
        assertsManager = Context.getInstance().getBean(AssertsManager.class);
    }

    protected WebDriver getDriver() {
        return Context.getInstance().getBean(DriverManager.class).getDriver();
    }

    protected <T extends AbstractPage> T getPage(Class<T> clazz) {
        return pageCatalog.getPage(clazz);
    }

    protected <T extends AbstractPage> T openPage(Class<T> clazz) {

        log.trace("Открываем страницу (" + clazz + ")");
        if (pageCatalog.getCurrentPage() != null
                && pageCatalog.getCurrentPage().getClass() == clazz) return (T) pageCatalog.getCurrentPage();
        try {
            log.info("Переходим на страницу " + clazz.getSimpleName());
            return openPageFromCurrentPage(clazz);
        } catch (RuntimeException ignore) {
            log.warn("Относительный переход не удался. Идём через главную страницу.");
            return openPageByFullPath(clazz);
        }
    }

    private <T extends AbstractPage> T openPageFromCurrentPage(Class<T> clazz) {
        if (pageCatalog.getCurrentPage() != null) {
            for (Method method : pageCatalog.getCurrentPage().getClass().getDeclaredMethods()) {
                if (clazz.equals(method.getGenericReturnType())
                        && method.getName().matches("(?i)open.+page")) {
                    try {
                        method.setAccessible(true);
                        log.info("Выполняем " + method.getName() + " из " + clazz.getSimpleName());
                        return (T) method.invoke(pageCatalog.getCurrentPage());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        throw new RuntimeException();
    }

    protected abstract <T extends AbstractPage> T openPageByFullPath(Class<T> clazz);

    protected AbstractPage getCurrentPage() {
        return pageCatalog.getCurrentPage();
    }

    protected void setCurrentPage(AbstractPage abstractPage) {
        pageCatalog.setCurrentPage(abstractPage);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getDataKeeper() {
        return (Map<String, Object>) Context.getInstance().getBean("dataKeeper");
    }

    protected void saveTestData(String key, Object value) {
        getDataKeeper().put(key, value);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getTestData(String key) {
        return (T) getDataKeeper().get(key);
    }

    protected void clearTestData() {
        getDataKeeper().clear();
    }

    @Deprecated
    protected <T extends AbstractPage> T initPage(Class<T> clazz) {
        return getPage(clazz);
    }
}
