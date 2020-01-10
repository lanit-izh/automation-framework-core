package ru.lanit.at.atlas.extension;

import io.qameta.atlas.core.internal.Configuration;
import io.qameta.atlas.core.util.MethodInfo;
import io.qameta.atlas.webdriver.context.WebDriverContext;
import io.qameta.atlas.webdriver.extension.ExecuteJScriptMethodExtension;
import ru.lanit.at.context.Context;
import ru.lanit.at.driver.DriverManager;


public class ExecuteJScript extends ExecuteJScriptMethodExtension {

    @Override
    public Object invoke(final Object proxy,
                         final MethodInfo methodInfo,
                         final Configuration configuration) {
        if (!configuration.getContext(WebDriverContext.class).isPresent()) {
            configuration.registerContext(new WebDriverContext(Context.getInstance().getBean(DriverManager.class).getDriver()));
        }
        return super.invoke(proxy, methodInfo, configuration);
    }
}