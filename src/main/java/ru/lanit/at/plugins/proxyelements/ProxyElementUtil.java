package ru.lanit.at.plugins.proxyelements;

import io.qameta.atlas.webdriver.AtlasWebElement;
import org.openqa.selenium.OutputType;
import ru.lanit.at.driver.DriverManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class ProxyElementUtil {
    /**
     * @param original
     * @param proxyClass Должен содержать конструктор принимающий 1 аргумент с тимо Object для перевода вызовов на оригинальный объект.
     * @param Interface
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public static Object WrapElement(Object original, Class proxyClass, Class Interface) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object proxyObject = proxyClass.getConstructor(Object.class).newInstance(original);
        return Proxy.newProxyInstance(
                Interface.getClassLoader(),
                new Class[]{Interface},
                (InvocationHandler) proxyObject);
    }

    /**
     * @param original
     * @param proxyClass Должен содержать конструктор принимающий 1 аргумент с тимо Object для перевода вызовов на оригинальный объект.
     *                   Или, если требуется два аргумента, второй с типом BufferedImage для передачи скриншота элемента.
     * @param Interface
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public static Object WrapElementWithScreenshot(Object original, BufferedImage screenshot, Class proxyClass, Class Interface) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object proxyObject = proxyClass.getConstructor(Object.class, BufferedImage.class).newInstance(original, screenshot);
        return Proxy.newProxyInstance(
                Interface.getClassLoader(),
                new Class[]{Interface},
                (InvocationHandler) proxyObject);
    }

    public static Object processAnotation(ProxyElement pe, AtlasWebElement origin, DriverManager dm, Class elementClass) {
        try {
            if (pe.initWithScreenshot()) {
                byte[] screenshot = origin.getScreenshotAs(OutputType.BYTES);
                ByteArrayInputStream bis = new ByteArrayInputStream(screenshot);
                return ProxyElementUtil.WrapElementWithScreenshot(origin, ImageIO.read(bis), ProxyClassLoader.getClass(pe.value()), elementClass);
            } else {
                return ProxyElementUtil.WrapElement(origin, ProxyClassLoader.getClass(pe.value()), elementClass);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | IOException e) {
            throw new IllegalStateException("Не получилось проксировать елемент " + pe.value(), e);
        }
    }
}
