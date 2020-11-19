package ru.lanit.at.plugins.proxyelements;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class ProxyElementUtil {
    /**
     *
     * @param original
     * @param proxyClass Должен содержать конструктор принимающий 1 аргумент с тимо Object для перевода вызовов на оригинальный объект
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
}
