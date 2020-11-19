package ru.lanit.at.plugins.proxyelements;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
/**
 * value - относительный путь до прокси класса, который будет проксирвать AtlasWebElement получаемы посредство аннотированного метода
 */
public @interface ProxyElement {
    String value();
}
