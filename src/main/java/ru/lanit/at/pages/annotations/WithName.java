package ru.lanit.at.pages.annotations;

import ru.lanit.at.pages.SearchBlockElement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates elements for searching in pageObject or in BlockElement by name.
 * Default api is {@link SearchBlockElement#getElement(java.lang.String, java.lang.Class, java.lang.String...)}
 *
 * @since 4.0.10
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithName {
    String[] value();
}
