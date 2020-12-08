package ru.lanit.at.plugins.proxyelements;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
/**
 * value - Имя класса, который будет использован для созднаия инстанса прокси для объекта.
 *
 * Аннотация применима к методам, проксируя их возвращаемое значение.
 * Если аннотация применена к классу, то все методы, которые возвращают значение с типом этого класса будут возвращать инстанс прокси.
 */
public @interface ProxyElement {
    String value();
    boolean initWithScreenshot() default false;
}
