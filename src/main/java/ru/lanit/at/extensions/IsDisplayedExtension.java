package ru.lanit.at.extensions;

import io.qameta.atlas.api.MethodExtension;
import io.qameta.atlas.internal.Configuration;
import io.qameta.atlas.util.MethodInfo;
import org.openqa.selenium.NoSuchElementException;

import java.lang.reflect.Method;

public class IsDisplayedExtension implements MethodExtension {
    @Override
    public Object invoke(Object o, MethodInfo methodInfo, Configuration configuration) throws Throwable {
        try {
            return methodInfo.getMethod().invoke(o);
        } catch (NoSuchElementException ignore) {
            return Boolean.FALSE;
        }
    }

    @Override
    public boolean test(Method method) {
        return method.getName().equalsIgnoreCase("isDisplayed")
                && method.getReturnType() == Boolean.TYPE
                && method.getGenericParameterTypes().length == 0;
    }
}
