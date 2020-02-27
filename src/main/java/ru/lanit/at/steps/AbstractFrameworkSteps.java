package ru.lanit.at.steps;

import com.consol.citrus.message.Message;
import io.qameta.atlas.core.Atlas;
import io.qameta.atlas.webdriver.AtlasWebElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import ru.lanit.at.citrus.CitrusRunner;
import ru.lanit.at.context.Context;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.exceptions.FrameworkRuntimeException;
import ru.lanit.at.pages.AbstractPage;
import ru.lanit.at.pages.PageCatalog;
import ru.lanit.at.pages.SearchBlockElement;
import ru.lanit.at.pages.block.AbstractBlockElement;
import ru.lanit.at.pages.element.UIElement;
import ru.lanit.at.utils.ScreenShooter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public abstract class AbstractFrameworkSteps {

    protected Logger log = LogManager.getLogger(this.getClass());
    private PageCatalog pageCatalog;
    private ScreenShooter screenShooter;
    private DriverManager driverManager;
    private Atlas atlas;
    private CitrusRunner citrusRunner;


    public AbstractFrameworkSteps() {
        ApplicationContext context = Context.getInstance();
        pageCatalog = context.getBean(PageCatalog.class);
        driverManager = context.getBean(DriverManager.class);
        atlas = pageCatalog.getAtlas();
        screenShooter = context.getBean(ScreenShooter.class);
        citrusRunner = context.getBean(CitrusRunner.class);
    }

    /**
     * @return Instance of {@link DriverManager}
     */
    protected DriverManager getDriverManager() {
        return driverManager;
    }

    /**
     * @return Instance of {@link WebDriver} which is active in current thread.
     */
    protected WebDriver getDriver() {
        return driverManager.getDriver();
    }

    /** Closes all driver windows and destroys WebDriver instance */
    protected void shutdownDriver() {
        driverManager.shutdown();
    }

    /**
     * Determines weather current WebDriver is null or not
     *
     * @return true if driver is not null.
     */
    protected boolean driverIsActive() {
        return driverManager.isActive();
    }

    /**
     * Returns instance of page with given class from {@link PageCatalog}.
     * If {@link PageCatalog} doesn't contain page with such page yet - it will be initialized and saved.
     *
     * @param clazz Class of page object that should be initialized and returned.
     * @return Instance of page object from {@link PageCatalog}.
     */
    protected <T extends AbstractPage> T getPage(Class<T> clazz) {
        return pageCatalog.getPage(clazz);
    }

    /**
     * Returns instance of page with given param from {@link PageCatalog}.
     * If {@link PageCatalog} doesn't contain page with such page yet - it will be initialized and saved.
     *
     * @param title Title value {@link ru.lanit.at.pages.annotations.Title}
     *              or className of page object that should be initialized and returned.
     * @return Instance of page object from {@link PageCatalog}.
     */
    protected <T extends AbstractPage> T getPageByTitle(String title) {
        return pageCatalog.getPageByTitle(title);
    }

    /**
     * Returns current active Page
     *
     * @return currentPage
     */
    protected AbstractPage getCurrentPage() {
        return pageCatalog.getCurrentPage();
    }

    /**
     * Initialized and set current block
     *
     * @param clazz  Class of block object that should be initialized and set  as  current block
     * @param params Parameters necessary for initialization
     */
    protected void setCurrentBlock(Class<? extends AbstractBlockElement> clazz, String... params) {
        Object object = getSearchContext();
        if (object == null) {
            throw new FrameworkRuntimeException("Для установки блока, требуется установить текущую страницу," +
                    " используя методы 'getPage/getPageByTitle'");
        }
        AbstractBlockElement abstractBlockElement;
        if (!AbstractPage.class.isAssignableFrom(object.getClass())) {
            abstractBlockElement = ((AbstractBlockElement) object).getBlockElement(clazz, params);
        } else {
            abstractBlockElement = ((AbstractPage) object).getBlockElement(clazz, params);
        }
        pageCatalog.setCurrentBlock(abstractBlockElement);
        log.info("В качестве текущего блока установлен: '" + clazz.getSimpleName() + "'");
    }

    /**
     * Initialized and set current block
     *
     * @param title  Title value {@link ru.lanit.at.pages.annotations.Title} or
     *               className of block object that should be initialized and set  as  current block
     * @param params Parameters necessary for initialization
     */
    protected void setCurrentBlockByName(String title, String... params) {
        Class<? extends AbstractBlockElement> abstractBlockElement = pageCatalog.findBlockByName(title);
        setCurrentBlock(abstractBlockElement, params);
    }

    /**
     * Returns current active block
     *
     * @return current Block
     */
    protected AbstractBlockElement getCurrentBlock() {
        return pageCatalog.getCurrentBlockElement();
    }


    /**
     * Returns instance of  UiElement
     *
     * @param clazz  Class of UiElement object that should be initialized
     * @param params Parameters necessary for initialization
     */
    @SuppressWarnings("unchecked")
    protected <T extends UIElement> T getUIElement(Class<? extends UIElement> clazz, String... params) {
        Object obj = getSearchContext();
        if (obj == null) {
            throw new FrameworkRuntimeException("Установите контекст поиска элемента на страницу/блок");
        }
        if (!AbstractPage.class.isAssignableFrom(obj.getClass())) {
            return (T) ((AbstractBlockElement) obj).getElement(clazz, params);
        }
        return (T) ((AbstractPage) obj).getElement(clazz, params);
    }

    @SuppressWarnings("unchecked")
    protected <T extends AtlasWebElement> T getElementByName(String elementName, Class<? extends AtlasWebElement> elementType, String... params) {
        Object context = getSearchContext();
        if (context == null)
            throw new FrameworkRuntimeException("You must select current block or page before using this method");
        return (T) ((SearchBlockElement) context).getElement(elementName, elementType, params);
    }

    /**
     * Returns instance of  UiElement
     *
     * @param nameElement Title value {@link ru.lanit.at.pages.annotations.Title} or
     *                    *              className of uiElement object that should be initialized
     * @param params      Parameters necessary for initialization
     */
    protected <T extends UIElement> T getUIElementByName(String nameElement, String... params) {
        Class<? extends UIElement> clazz = pageCatalog.findUIElemByName(nameElement);
        return getUIElement(clazz, params);
    }

    /**
     * Execute methods by method name
     *
     * @param object     Object  whose methods will be executed
     * @param methodName Method simple name
     * @param params     Parameters necessary for execute
     * @deprecated will be deleted soon. Use concrete steps for each type of elements
     */
    protected void executeMethodByName(Object object, String methodName, Object... params) {
        Method[] methods = object.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equalsIgnoreCase(methodName.trim())) {
                try {
                    method.invoke(object, params);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new FrameworkRuntimeException("Не удалось выполнить метод :'" + methodName + "'");
                }
                return;
            }
        }
        throw new FrameworkRuntimeException("Не найден метод: '" + methodName + "', в объекте: '" + object + "'.");
    }

    /**
     * Returns an instance of the current search context or page or block
     *
     * @return instance page or block
     */
    protected Object getSearchContext() {
        if (pageCatalog.getCurrentBlockElement() == null) {
            return pageCatalog.getCurrentPage();
        }
        return pageCatalog.getCurrentBlockElement();
    }

    /** Reset Current Block */
    public void resetCurrentBlock() {
        pageCatalog.setCurrentBlock(null);
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
        log.info("Saved text data {}: {}", key, value);
        getDataKeeper().put(key, value);
    }


    /**
     * Returns saved test data by specified key. Automatically casts object into requested data type.
     *
     * @param key - key
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
     * Method to retrieve and interact with instance of {@link Atlas}.
     *
     * @return {@link Atlas} bean.
     */
    public Atlas getAtlas() {
        return atlas;
    }

    /**
     * Returns an instance of PageCatalog  {@link PageCatalog}
     *
     * @return instance PageCatalog
     */
    protected PageCatalog getPageCatalog() {
        return pageCatalog;
    }

    public ScreenShooter getScreenShooter() {
        return screenShooter;
    }


    /**
     * Returns an instance of CitrusRunner  {@link CitrusRunner}
     *
     * @return instance CitrusRunner
     */
    public CitrusRunner getCitrusRunner() {
        return citrusRunner;
    }

    /**
     * Returns an Object of Endpoint
     *
     * @param name - Endpoint Name (@link org.springframework.context.annotation.Bean#name())
     * @return object of Endpoint
     */
    public Object getEndpointByName(String name) {
        return citrusRunner.getTestContext().getApplicationContext().getBean(name);
    }

    /**
     * Returns an Object of Endpoint
     *
     * @param requiredType type the bean must match; can be an interface or superclass
     *                     return an instance of the single bean matching the required type
     */
    public <T> T getEndpointByClassName(Class<T> requiredType) {
        return citrusRunner.getTestContext().getApplicationContext().getBean(requiredType);
    }


    public <T> void setCitrusVariable(String key, T value) {
        citrusRunner.variable(key, value);
    }


    /**
     * Gets the value for the given variable expression. Expression usually is the
     * simple variable name, with optional expression prefix/suffix.
     * <p>
     * In case variable is not known to the context throw runtime exception.
     *
     * @param key expression to search for.
     * @return value of the variable
     */
    public String getCitrusVariable(String key) {
        return citrusRunner.getTestContext().getVariable(key);
    }

    /**
     * Gets typed variable value.
     *
     * @param key  expression to search for.
     * @param type Class type
     * @return typed variable value
     */
    public <T> T getCitrusVariable(String key, Class<T> type) {
        return citrusRunner.getTestContext().getVariable(key, type);
    }

    /**
     * Gets the value for the given variable as object representation.
     * Use this method if you seek for test objects stored in the context.
     *
     * @param variableExpression expression to search for.
     * @return value of the variable as object
     */
    public Object getVariableObject(String variableExpression) {
        return citrusRunner.getTestContext().getVariable(variableExpression);
    }

    /**
     * Gets message by name.
     *
     * @param name message id.
     * @return message Object
     */
    public Message getMessageByName(String name) {
        return citrusRunner.getTestContext().getMessageStore().getMessage(name);
    }
}
