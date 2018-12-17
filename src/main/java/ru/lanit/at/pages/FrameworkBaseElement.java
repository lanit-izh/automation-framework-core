package ru.lanit.at.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.lanit.at.context.Context;
import ru.lanit.at.make.Make;
import ru.lanit.at.make.Wait;

public interface FrameworkBaseElement {

    default Logger log() {
        return LogManager.getLogger(getClass());
    }

    /**
     * Method to interact with waiters.
     *
     * @return Instance of {@link Wait}
     */
    default Wait await() {
        return Context.getInstance().getBean(Wait.class);
    }


    /**
     * Method to interract with makers.
     *
     * @return Instance of {@link Make}
     */
    default Make make() {
        return Context.getInstance().getBean(Make.class);
    }


    default PageCatalog pageCatalog() {
        return Context.getInstance().getBean(PageCatalog.class);
    }

    /**
     * Returns instance of page with given class from {@link PageCatalog}. If {@link PageCatalog} doesn't contain page with such page yet - it will be initialized and saved.
     *
     * @param clazz Class of page object that should be initialized and returned.
     * @return Instance of page object from {@link PageCatalog}.
     */
    default <T extends AbstractPage> T getPage(Class<T> clazz) {
        return pageCatalog().getPage(clazz);
    }
}
