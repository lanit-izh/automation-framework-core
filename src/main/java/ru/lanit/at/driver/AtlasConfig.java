package ru.lanit.at.driver;

import io.qameta.atlas.core.internal.Configuration;
import io.qameta.atlas.core.internal.DefaultMethodExtension;
import io.qameta.atlas.webdriver.context.WebDriverContext;
import io.qameta.atlas.webdriver.extension.DriverProviderExtension;
import io.qameta.atlas.webdriver.extension.ExecuteJScriptMethodExtension;
import io.qameta.atlas.webdriver.extension.FilterCollectionExtension;
import io.qameta.atlas.webdriver.extension.FindByCollectionExtension;
import io.qameta.atlas.webdriver.extension.FindByExtension;
import io.qameta.atlas.webdriver.extension.PageExtension;
import io.qameta.atlas.webdriver.extension.ShouldMethodExtension;
import io.qameta.atlas.webdriver.extension.WaitUntilMethodExtension;
import io.qameta.atlas.webdriver.extension.WrappedElementMethodExtension;
import org.openqa.selenium.WebDriver;


public class AtlasConfig extends Configuration {

    public AtlasConfig(final WebDriver webDriver) {
        registerContext(new WebDriverContext(webDriver));
        registerExtension(new DriverProviderExtension());
        registerExtension(new DefaultMethodExtension());
        registerExtension(new FindByExtension());
        registerExtension(new FindByCollectionExtension());
        registerExtension(new ShouldMethodExtension());
        registerExtension(new WaitUntilMethodExtension());
        registerExtension(new WrappedElementMethodExtension());
        registerExtension(new ExecuteJScriptMethodExtension());
        registerExtension(new PageExtension());
        registerExtension(new FilterCollectionExtension());
    }
}
