package ru.lanit.at.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanit.at.exceptions.FrameworkRuntimeException;
import ru.lanit.at.pages.block.AbstractBlockElement;
import ru.lanit.at.pages.element.UIElement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface SearchBlockElement {
    Logger LOGGER = LoggerFactory.getLogger(SearchBlockElement.class);

    default <T extends AbstractBlockElement> T getBlockElement(Class<T> blockClass, String... params) {
        Method method = findBlock(this.getClass(), blockClass, params);
        if (method == null) {
            throw new FrameworkRuntimeException("Текущая страница/блок '" + this.getClass().getInterfaces()[0].getSimpleName() +
                    ", не содержит блок/элемент + '" + blockClass.getSimpleName() + "'. Добавьте блок в искомую страниц/блок для работы с ним");
        }
        return init(method, this, params);
    }

    default <T extends UIElement> T getElement(Class<T> blockClass, String... params) {
        Method method = findBlock(this.getClass(), blockClass, params);
        if (method == null) {
            throw new FrameworkRuntimeException("Текущая страница/блок '" + this.getClass().getInterfaces()[0].getSimpleName() +
                    ", элемент + '" + blockClass.getSimpleName() + "'. Добавьте элемент в искомую страниц/блок для работы с ним");
        }
        return init(method, this, params);
    }


    default Method findBlock(Class currentClass, Class blockClass, String... params) {
        Method[] methods = currentClass.getInterfaces()[0].getMethods();
        List<Method> blocks = Stream.of(methods).filter(method -> blockClass.isAssignableFrom(method.getReturnType()) && method.getGenericParameterTypes().length == params.length).collect(Collectors.toList());
        if (blocks.isEmpty()) {
            return null;
        }
        if (blocks.size() != 1) {
            LOGGER.info("Блок '" + blockClass.getSimpleName() + "' подключен к текущей странице/блоку'" + currentClass.getInterfaces()[0].getSimpleName() + "' более 1 раза ");
        }
        return blocks.get(0);
    }

    default <T> T init(Method method, Object object, String... params) {
        try {
            Type[] param = method.getGenericParameterTypes();
            if (param.length == params.length) {
                return (T) method.invoke(object, params);
            } else {
                throw new FrameworkRuntimeException("Для создания '" + method.getReturnType().getSimpleName() + " 'необходимое количество параметров: " + param.length + ", передано параметров: " + params.length);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new FrameworkRuntimeException("Не удалось создать блок/элемент :'" + method.getReturnType().getSimpleName() + "'");
        }
    }


}
