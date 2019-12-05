package ru.lanit.at.citrus;

//import com.consol.citrus.Citrus;

import com.consol.citrus.Citrus;
import com.consol.citrus.dsl.runner.DefaultTestRunner;

public class CitrusRunner extends DefaultTestRunner {

    public CitrusRunner(Citrus citrus) {
        super(citrus.getApplicationContext(), citrus.createTestContext());
    }
}