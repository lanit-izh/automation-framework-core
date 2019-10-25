package ru.lanit.at.pages;

import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import io.qameta.atlas.core.Atlas;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.exceptions.FrameworkRuntimeException;
import ru.lanit.at.pages.annotations.Title;
import ru.lanit.at.pages.block.AbstractBlockElement;
import ru.lanit.at.pages.element.UIElement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * Catalog that initializes, saves and provides PageObjects which are required during the test run.
 */

public class PageCatalog {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageCatalog.class);
    private static final String PACKAGE = "pages";
    private List<AbstractPage> pageList = new LinkedList<>();

    private Collection<Class<? extends AbstractPage>> abstractPageChildCollection = getAbstractPageDescendants();
    private Collection<Class<? extends AbstractBlockElement>> abstractBlockChildCollection = getAbstractBlockDescendants();
    private Collection<Class<? extends UIElement>> UIElementChildCollection = getFrameworkBaseElementDescendants();


    private DriverManager driverManager;
    private WebDriver previousDriver;
    private Atlas atlas;

    /**
     * Variable for tracking which page object/block is currently opened and in use.
     */
    private AbstractPage currentPage;
    private AbstractBlockElement currentAbstractBlockElement;


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
            T page = atlas.create(actualDriver, clazz);
            setCurrentPage(page);
            pageList.add(page);
            return page;
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

    public void setCurrentBlock(AbstractBlockElement block) {
        this.currentAbstractBlockElement = block;
    }


    public WebDriver getDriver() {
        return driverManager.getDriver();
    }

    public void setDriverManager(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    public Atlas getAtlas() {
        return atlas;
    }

    public void setAtlas(Atlas atlas) {
        this.atlas = atlas;
    }


    @SuppressWarnings("unchecked")
    public <T extends AbstractPage> T getPageByTitle(String name) {
        LOGGER.debug("Find page by name '" + name + "'");
        Class<? extends AbstractPage> requiredPageClass = tryToFind(name, abstractPageChildCollection);
        if (requiredPageClass == null) {
            throw new FrameworkRuntimeException("Requested page with className or title '" + name
                    + "' not found in package '" + PACKAGE + "'.");
        }
        return (T) getPage(requiredPageClass);
    }

    public Class<? extends AbstractBlockElement> findBlockByName(String name) {
        LOGGER.debug("Find block by name '" + name + "'");
        Class<? extends AbstractBlockElement> requiredBlock = tryToFind(name, abstractBlockChildCollection);
        if (requiredBlock == null) {
            throw new FrameworkRuntimeException("Requested block with name '" + name
                    + "' not found in package '" + PACKAGE + "'.");
        }
        return requiredBlock;
    }

    public Class<? extends UIElement> findUIElemByName(String name) {
        LOGGER.debug("Find element by name '" + name + "'");
        Class<? extends UIElement> element = tryToFind(name, UIElementChildCollection);
        if (element == null) {
            throw new FrameworkRuntimeException("Requested element with name '" + name
                    + "' not found in package'" + PACKAGE + "'.");
        }
        return element;
    }

    private <T> Class<? extends T> tryToFind(String name, Collection<Class<? extends T>> collection) {
        List<Class<? extends T>> pagesList = collection.stream()
                .filter(pageClass -> titleEquals(pageClass, name)).collect(Collectors.toList());
        if (pagesList.isEmpty()) {
            return null;
        }
        if (pagesList.size() != 1) {
            LOGGER.error("Search contains more than one result for param :'" + name
                    + "'.  Check your title for uniqueness");
        }
        return pagesList.get(0);
    }


    private <T> boolean titleEquals(Class<T> pageClass, String name) {
        Title title = pageClass.getAnnotation(Title.class);
        if (title != null) {
            String[] titles = title.values();
            for (String t : titles) {
                if (t.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }


    public AbstractBlockElement getCurrentBlockElement() {
        return currentAbstractBlockElement;
    }

//
//    public <T extends AbstractBlockElement> void setCurrentBlock(Object ob, Class<T> blockClass, String... params) {
//        LOGGER.info("В качестве текущего контекста поиска установлен блок '" + blockClass.getSimpleName() + "'");
//        this.currentAbstractBlockElement = getBlockElement(ob, blockClass, params);
//    }
//
//
//
//    public <T> T getBlockElement(Object ob, Class<T> blockClass, String... params) {
//        Method method = findBlock(ob.getClass(), blockClass, params);
//        if (method == null) {
//            throw new FrameworkRuntimeException("Текущая страница/блок '" + ob.getClass().getInterfaces()[0].getSimpleName() +
//                    ", не содержит блок/элемент + '" + blockClass.getSimpleName() + "'. Добавьте блок в искомую страниц/блок для работы с ним");
//        }
//        return init(method, ob, params);
//    }
//
//
//    private Method findBlock(Class currentClass, Class blockClass, String... params) {
//        Method[] methods = currentClass.getInterfaces()[0].getMethods();
//        List<Method> blocks = Stream.of(methods).filter(method -> blockClass.isAssignableFrom(method.getReturnType()) && method.getGenericParameterTypes().length == params.length).collect(Collectors.toList());
//        if (blocks.isEmpty()) {
//            return null;
//        }
//        if (blocks.size() != 1) {
//            LOGGER.error("Блок/элемент '" + blockClass.getSimpleName() + "' подключен к текущей странице/блоку'" + currentClass.getInterfaces()[0].getSimpleName() + "' более 1 раза ");
//        }
//        return blocks.get(0);
//    }
//
//    private <T> T init(Method method, Object object, String... params) {
//        try {
//            Type[] param = method.getGenericParameterTypes();
//            if (param.length == params.length) {
//                return (T) method.invoke(object, params);
//            } else {
//                throw new FrameworkRuntimeException("Для создания '" + method.getReturnType().getSimpleName() + " 'необходимое количество параметров: " + param.length + ", передано параметров: " + params.length);
//            }
//        } catch (IllegalAccessException | InvocationTargetException e) {
//            throw new FrameworkRuntimeException("Не удалось создать блок/элемент: " + method.getReturnType().getSimpleName());
//        }
//    }


    private Collection<Class<? extends AbstractPage>> getAbstractPageDescendants() {
        return getDescendants(AbstractPage.class);
    }

    private Collection<Class<? extends AbstractBlockElement>> getAbstractBlockDescendants() {
        Collection<Class<? extends AbstractBlockElement>> blocks = getDescendants(AbstractBlockElement.class);
        blocks.removeAll(abstractPageChildCollection);
        return blocks;
    }


    private Collection<Class<? extends UIElement>> getFrameworkBaseElementDescendants() {
        Collection<Class<? extends UIElement>> blocks = getDescendants(UIElement.class);
        blocks.removeAll(abstractPageChildCollection);
        blocks.removeAll(abstractBlockChildCollection);
        return blocks;
    }

    private <T> Collection<Class<? extends T>> getDescendants(Class<T> blockClass) {
        ClassLoader classLoader = getClass().getClassLoader();
        ResourceLoaderClassFinder resourceLoaderClassFinder =
                new ResourceLoaderClassFinder(new MultiLoader(classLoader), classLoader);
        return resourceLoaderClassFinder.getDescendants(blockClass, PACKAGE);
    }
}
