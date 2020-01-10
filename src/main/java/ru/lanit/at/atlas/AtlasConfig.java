package ru.lanit.at.atlas;

import io.qameta.atlas.core.internal.Configuration;
import io.qameta.atlas.core.internal.DefaultMethodExtension;
import io.qameta.atlas.webdriver.extension.DriverProviderExtension;
import io.qameta.atlas.webdriver.extension.FilterCollectionExtension;
import io.qameta.atlas.webdriver.extension.FindByCollectionExtension;
import io.qameta.atlas.webdriver.extension.FindByExtension;
import io.qameta.atlas.webdriver.extension.PageExtension;
import io.qameta.atlas.webdriver.extension.ShouldMethodExtension;
import io.qameta.atlas.webdriver.extension.ToStringMethodExtension;
import io.qameta.atlas.webdriver.extension.WaitUntilMethodExtension;
import io.qameta.atlas.webdriver.extension.WrappedElementMethodExtension;
import ru.lanit.at.atlas.extension.ExecuteJScript;


public class AtlasConfig extends Configuration {

    public AtlasConfig() {
        registerExtension(new DriverProviderExtension());
        registerExtension(new DefaultMethodExtension());
        registerExtension(new ToStringMethodExtension());
        registerExtension(new FindByExtension());
        registerExtension(new FindByCollectionExtension());
        registerExtension(new ShouldMethodExtension());
        registerExtension(new WaitUntilMethodExtension());
        registerExtension(new WrappedElementMethodExtension());
        registerExtension(new ExecuteJScript());
        registerExtension(new PageExtension());
        registerExtension(new FilterCollectionExtension());
    }
}
