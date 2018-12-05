package ru.lanit.at.extensions;

import io.qameta.atlas.Atlas;
import io.qameta.atlas.api.MethodExtension;
import io.qameta.atlas.internal.Configuration;
import io.qameta.atlas.util.MethodInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import java.lang.reflect.Method;

import static java.lang.String.format;

/**
 * define new annotation @ContainClass
 * forming short xpath using attribute
 */
public class ContainsClassExtension implements MethodExtension {

    @Override
    public boolean test(Method method) {
        return method
                .isAnnotationPresent(ContainsClass.class);
    }

    @Override
    public Object invoke(Object proxy, MethodInfo methodInfo, Configuration configuration) {
        assert proxy instanceof SearchContext;
        String classNmae = methodInfo.getMethod().getAnnotation(ContainsClass.class).value();
        String xpath = format(".//*[contains(@class, '%s')]", classNmae);
        SearchContext context = (SearchContext) proxy;
        return new Atlas().create(context.findElement(By.xpath(xpath)), methodInfo.getMethod().getReturnType());
    }

}
