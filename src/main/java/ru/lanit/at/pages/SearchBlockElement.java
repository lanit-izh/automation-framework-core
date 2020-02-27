package ru.lanit.at.pages;


import io.qameta.atlas.webdriver.AtlasWebElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.lanit.at.exceptions.FrameworkRuntimeException;
import ru.lanit.at.pages.annotations.WithName;
import ru.lanit.at.pages.block.AbstractBlockElement;
import ru.lanit.at.pages.element.UIElement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.stream.Stream;

/**
 * Utility interface for searching elements at extending it pages
 */
public interface SearchBlockElement {
    Logger LOGGER = LogManager.getLogger(SearchBlockElement.class);

    /**
     * Searches and creates block element from current page 'this' object.
     *
     * @param blockClass desired type of block element
     * @param params     vararg of desired block element method creation
     * @return created element
     * @throws FrameworkRuntimeException in cases when no such method found or in cases problems with method invocation
     */
    default <T extends AbstractBlockElement> T getBlockElement(Class<T> blockClass, String... params) {
        Method method = findElement(this.getClass(), blockClass, params);
        if (method == null) {
            throw new FrameworkRuntimeException("Current page/block '" + this.getClass().getInterfaces()[0].getSimpleName() +
                    ", not contains block/element + '" + blockClass.getSimpleName() + "'. Please, add that block into page/block");
        }
        return init(method, this, params);
    }

    /**
     * Searches and creates end-point page element.
     * Works similar with {@link #getBlockElement}
     *
     * @param elementClass desired type of element
     * @param params       vararg of desired element method creation
     * @return created element
     * @throws FrameworkRuntimeException in cases when no such method found or in cases problems with method invocation
     */
    default <T extends UIElement> T getElement(Class<T> elementClass, String... params) {
        Method method = findElement(this.getClass(), elementClass, params);
        if (method == null) {
            throw new FrameworkRuntimeException("Current page/block '" + this.getClass().getInterfaces()[0].getSimpleName() +
                    ", not contains element '" + elementClass.getSimpleName() + "'. Please, add that element into page/block");
        }
        return init(method, this, params);
    }

    /**
     * Searches and creates end-point page element by {@link ru.lanit.at.pages.annotations.WithName} annotation.
     * Can be used either elements or blockedElements
     *
     * @param elementName  case-insensitive name of element, used in method annotation
     * @param elementClass desired type of element
     * @param params       vararg of desired element method creation
     * @return created element
     * @throws FrameworkRuntimeException in cases when no such method found or in cases problems with method invocation
     * @since 4.0.10
     */
    default <T extends AtlasWebElement<?>> T getElement(String elementName, Class<T> elementClass, String... params) {
        Method method = findElement(elementName, this.getClass(), elementClass, params);
        return init(method, this, params);
    }


    /**
     * Searches method by given elementName, then checks that one of them returns desirable type and returns first of it or
     * throws FrameworkRuntimeException if nothing appropriate found
     *
     * @param parentClass  parent Class of object, where searching appropriate method
     * @param elementClass searched element Class
     * @param params       varargs params of desired method
     * @return first suitable method or FrameworkRuntimeException
     * @throws FrameworkRuntimeException when no one method with given name, return type and parameters count
     * @sinse 4.0.10
     */
    default Method findElement(String elementName, Class<?> parentClass, Class<?> elementClass, String... params) {
        Method[] methods = parentClass.getInterfaces()[0].getMethods();
        return Stream.of(methods)
                .filter(method -> {
                    if (method.isAnnotationPresent(WithName.class)) {
                        for (String name : method.getAnnotation(WithName.class).value()) {
                            if (name.equalsIgnoreCase(elementName)) return true;
                        }
                    }
                    return false;
                })
                .filter(method -> elementClass.isAssignableFrom(method.getReturnType())
                        && method.getGenericParameterTypes().length == params.length)
                .findFirst()
                .orElseThrow(() -> new FrameworkRuntimeException("No method with return type '" + elementClass.getCanonicalName() + "' " +
                        "or appropriate parameters count '" + params.length + "' " +
                        "or with name '" + elementName + "'"));
    }


    /**
     * Searches method in class of provided object Class, which creates given elementClass. it finds it by
     * return type and method parameters count.
     * Was renamed from <code>findBlock</code> to <code>findElement</code> for clarify
     *
     * @param parentClass  parent Class of object, where searching appropriate method
     * @param elementClass searched element Class
     * @param params       varargs params of desired method
     * @return first suitable method or <code>null</code> if there are no appropriate method
     */
    default Method findElement(Class<?> parentClass, Class<?> elementClass, String... params) {
        Method[] methods = parentClass.getInterfaces()[0].getMethods();
        return Stream.of(methods)
                .filter(method -> elementClass.isAssignableFrom(method.getReturnType())
                        && method.getGenericParameterTypes().length == params.length)
                .findFirst()
                .orElse(null);
    }

    /**
     * Invokes given method which creates some page element with given parameters at given page/block.
     * Wraps arise exception if it happened into {@link ru.lanit.at.exceptions.FrameworkRuntimeException}
     *
     * @return created page element
     * @throws ru.lanit.at.exceptions.FrameworkRuntimeException when there are problems in method invocation
     */
    @SuppressWarnings("unchecked")
    default <T> T init(Method method, Object blockElement, String... params) {
        try {
            Type[] param = method.getGenericParameterTypes();
            if (param.length == params.length) {
                return (T) method.invoke(blockElement, params);
            } else {
                throw new FrameworkRuntimeException("For creation '" + method.getReturnType().getSimpleName() + "' must be: " + param.length + ", but was: " + params.length);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new FrameworkRuntimeException("Attempt of creating block/element failed:'" + method.getReturnType().getSimpleName() + "'", e);
        }
    }
}
