package ru.lanit.at.steps;

import io.qameta.allure.Attachment;
import io.qameta.atlas.Atlas;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import ru.lanit.at.assertion.AssertsManager;
import ru.lanit.at.context.Context;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.exceptions.FrameworkRuntimeException;
import ru.lanit.at.pages.AbstractPage;
import ru.lanit.at.pages.PageCatalog;
import ru.lanit.at.pages.optionals.OptionalPageInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public abstract class AbstractFrameworkSteps {

    protected Logger log = LogManager.getLogger(this.getClass());
    protected AssertsManager assertsManager;
    private PageCatalog pageCatalog;
    private DriverManager driverManager;
    private Atlas atlas;

    public AbstractFrameworkSteps() {
        ApplicationContext context = Context.getInstance();
        pageCatalog = context.getBean(PageCatalog.class);
        assertsManager = context.getBean(AssertsManager.class);
        driverManager = context.getBean(DriverManager.class);
        atlas = context.getBean(Atlas.class);
    }

    /**
     * @return Instance of {@link WebDriver} which is active in current thread.
     */
    protected WebDriver getDriver() {
        return driverManager.getDriver();
    }

    protected void shutdownDriver() {
        driverManager.shutdown();
    }

    protected boolean driverIsActive() {
        return driverManager.isActive();
    }

    /**
     * Returns instance of page with given class from {@link PageCatalog}. If {@link PageCatalog} doesn't contain page with such page yet - it will be initialized and saved.
     *
     * @param clazz Class of page object that should be initialized and returned.
     * @return Instance of page object from {@link PageCatalog}.
     */
    protected <T extends AbstractPage> T getPage(Class<T> clazz) {
        return pageCatalog.getPage(clazz);
    }

    @SuppressWarnings("unchecked")
    protected <T extends AbstractPage> T openPage(Class<T> clazz) {
        log.trace("Open page (" + clazz + ")");
        if (pageCatalog.getCurrentPage() != null
                && pageCatalog.getCurrentPage().getClass() == clazz) return (T) pageCatalog.getCurrentPage();
        try {
            log.info("Going to page " + clazz.getSimpleName());
            return openPageFromCurrentPage(clazz);
        } catch (RuntimeException ignore) {
            log.warn("Relative transition to page failed. Going through the main page.");
            return openPageByFullPath(clazz);
        }
    }

    /**
     * Pseudo-smart method that tries to open requested page from current page.
     * Searches method of current page' page object with method name satisfying regexp: {@code (?i)open.+page} (case insensitive) and also method should return instance of requested class.
     *
     * @param clazz Class of page object that should be initialized and returned.
     * @return Instance of requested page object if page can be opened from current page.
     * @throws FrameworkRuntimeException when there is no method to open requested page from current page.
     */
    @SuppressWarnings("unchecked")
    protected <T extends AbstractPage> T openPageFromCurrentPage(Class<T> clazz) {
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
        throw new FrameworkRuntimeException("Can't open page " + clazz.getSimpleName() + " from current page: " + pageCatalog.getCurrentPage().getClass().getSimpleName());
    }

    /**
     * Method which guarantees that requested page will be opened and initialized.
     *
     * @param clazz Class of the page that should be opened and initialized.
     * @return Instance of page object.
     */
    protected abstract <T extends AbstractPage> T openPageByFullPath(Class<T> clazz);

    protected AbstractPage getCurrentPage() {
        return pageCatalog.getCurrentPage();
    }

    /**
     * Method to force update of current page.
     *
     * @param abstractPage Page that should be set as current page.
     */
    protected void setCurrentPage(AbstractPage abstractPage) {
        pageCatalog.setCurrentPage(abstractPage);
    }

    /**
     * @return Data keeper object as map.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getDataKeeper() {
        return (Map<String, Object>) Context.getInstance().getBean("dataKeeper");
    }

    /**
     * Saves any test data which can be accessible during all test runtime by specified key.
     *
     * @param key   Unique key to save test data.
     * @param value Any data that should be saved.
     */
    protected void saveTestData(String key, Object value) {
        if (value instanceof CharSequence || value instanceof Number) logToAllure("Saved text data", "Key: \"" + key + "\", value: \"" + value + "\"");
        getDataKeeper().put(key, value);
    }

    @Attachment(value = "{0}", type = "text/plain")
    protected String logToAllure(String messageType, String message) {
        log.info("{}: {}", messageType, message);
        return message;
    }

    /**
     * Returns saved test data by specified key. Automatically casts object into requested data type.
     *
     * @param key
     * @param <T> Automatically casts saved object to requested type.
     *            E.g. {@code Foo foo = getTestData("foo");}
     * @return Saved value by given key or {@code null} if there is no saved data with given key.
     * @throws ClassCastException In case when value by given key can't be cast into requested type.
     */
    @SuppressWarnings("unchecked")
    protected <T> T getTestData(String key) {
        return (T) getDataKeeper().get(key);
    }

    /**
     * Erases all saved test data.
     */
    protected void clearTestData() {
        getDataKeeper().clear();
    }

    /**
     * Attempts to cast {@link PageCatalog#getCurrentPage()} to optional interface.
     *
     * @param iClass On of the descendants of {@link OptionalPageInterface}. Which indicates that current page should have requested capabilities.
     * @return {@link PageCatalog#getCurrentPage()} casted into given optional interface.
     * @throws ClassCastException In case when current page doesn't implement given interface.
     */
    @SuppressWarnings("unchecked")
    protected <I extends OptionalPageInterface> I transformCurrentPageTo(Class<I> iClass) {
        return (I) getCurrentPage();
    }

    /**
     * Method to retrieve and interact with instance of {@link Atlas}.
     * @return {@link Atlas} bean.
     */
    public Atlas getAtlas() {
        return atlas;
    }

    /**
     * Method to get instance of page.
     *
     * @param clazz class of page that should be instantiated.
     * @return instance of clazz.
     * @deprecated Use getPage(...) method.
     */
    @Deprecated
    protected <T extends AbstractPage> T initPage(Class<T> clazz) {
        return getPage(clazz);
    }
}
