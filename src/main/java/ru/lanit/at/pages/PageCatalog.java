package ru.lanit.at.pages;

import io.qameta.atlas.core.Atlas;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.reflections.Reflections;
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
    private static final Logger LOGGER = LogManager.getLogger(PageCatalog.class.getName());
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
            LOGGER.info("Текущая страница : '" + abstractPage.getClass().getInterfaces()[0].getSimpleName() + "'");
        }
    }

    /**
     * Setup current block.
     *
     * @param block Block object that should be set as current block.
     */
    public void setCurrentBlock(AbstractBlockElement block) {
        this.currentAbstractBlockElement = block;
    }

    /**
     * Returns current block.
     *
     * @return current block.
     */
    public AbstractBlockElement getCurrentBlockElement() {
        return currentAbstractBlockElement;
    }

    /**
     * Returns current driver.
     *
     * @return driver.
     */
    public WebDriver getDriver() {
        return driverManager.getDriver();
    }

    public void setDriverManager(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    /**
     * Returns atlas.
     *
     * @return atlas.
     */
    public Atlas getAtlas() {
        return atlas;
    }

    public void setAtlas(Atlas atlas) {
        this.atlas = atlas;
    }


    /**
     * Returns instance of requested page object class from catalog. If catalog doesn't have such instance - initializes it and saves in catalog.
     *
     * @param name Title {@link ru.lanit.at.pages.annotations.Title} or ClassName  of page object to find or initialize in catalog.
     * @return Instance of clazz.
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractPage> T getPageByTitle(String name) {
        LOGGER.debug("Find page by name '" + name + "'");
        Class<? extends AbstractPage> requiredPageClass = tryToFind(name, abstractPageChildCollection);
        if (requiredPageClass == null) {
            throw new FrameworkRuntimeException("Requested page with className or name '" + name
                    + "' not found in package '" + PACKAGE + "'.");
        }
        return (T) getPage(requiredPageClass);
    }

    /**
     * Returns block class
     *
     * @param name BlockName {@link ru.lanit.at.pages.annotations.Title} or ClassName of block object to find .
     * @return clazz block.
     */

    public Class<? extends AbstractBlockElement> findBlockByName(String name) {
        LOGGER.debug("Find block by name '" + name + "'");
        Class<? extends AbstractBlockElement> requiredBlock = tryToFind(name, abstractBlockChildCollection);
        if (requiredBlock == null) {
            throw new FrameworkRuntimeException("Requested block with name '" + name
                    + "' not found in package '" + PACKAGE + "'.");
        }
        return requiredBlock;
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
     * Returns UiElement class
     *
     * @param name BlockName {@link ru.lanit.at.pages.annotations.Title} or ClassName of block object to find .
     * @return clazz UiElement.
     */
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
                .filter(pageClass -> titleOrClassNameEquals(pageClass, name)).collect(Collectors.toList());
        if (pagesList.isEmpty()) {
            return null;
        }
        if (pagesList.size() != 1) {
            LOGGER.error("Search contains more than one result for param :'" + name
                    + "'.  Check your title for uniqueness");
        }
        return pagesList.get(0);
    }


    private <T> boolean titleOrClassNameEquals(Class<T> pageClass, String name) {
        Title title = pageClass.getAnnotation(Title.class);
        if (title != null) {
            return title.value().equalsIgnoreCase(name.trim());
        }
        return pageClass.getSimpleName().equalsIgnoreCase(name.trim());
    }

    private Collection<Class<? extends AbstractPage>> getAbstractPageDescendants() {
        return getDescendants(AbstractPage.class);
    }

    private Collection<Class<? extends AbstractBlockElement>> getAbstractBlockDescendants() {
        Collection<Class<? extends AbstractBlockElement>> blocks = getDescendants(AbstractBlockElement.class);
        blocks.removeAll(abstractPageChildCollection);
        return blocks;
    }


    private Collection<Class<? extends UIElement>> getFrameworkBaseElementDescendants() {
        Collection<Class<? extends UIElement>> elements = getDescendants(UIElement.class);
        elements.removeAll(abstractPageChildCollection);
        elements.removeAll(abstractBlockChildCollection);
        return elements;
    }

    private <T> Collection<Class<? extends T>> getDescendants(Class<T> blockClass) {
        Reflections reflections = new Reflections(PACKAGE);
        return reflections.getSubTypesOf(blockClass);
    }
}
