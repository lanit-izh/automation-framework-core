package ru.lanit.at.assertion;

import io.qameta.allure.Attachment;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;
import org.testng.collections.Maps;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.util.ScreenshotUtils;

import java.util.Map;

public class ExtendedAssert extends SoftAssert {
    private final Map<AssertionError, IAssert<?>> m_errors = Maps.newLinkedHashMap();

    private Boolean isCritical = false;
    private DriverManager driverManager;

    public void setDriverManager(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    public void setCritical() {
        isCritical = true;
    }

    @Override
    protected void doAssert(IAssert<?> a) {
        onBeforeAssert(a);
        try {
            a.doAssert();
            onAssertSuccess(a);
        } catch (AssertionError ex) {
            onAssertFailure(a, ex);
            m_errors.put(ex, a);
            attachErrorMsg(ex);
            ScreenshotUtils.takeScreenshot(driverManager.getDriver());
            if (isCritical) {
                this.assertAll();
            }
        } finally {
            this.isCritical = false;
            onAfterAssert(a);
        }
    }

    @Attachment(value = "Error message")
    private String attachErrorMsg(AssertionError assertionError) {
        return assertionError.getLocalizedMessage();
    }

    @Override
    public void assertAll() {
        if (!m_errors.isEmpty()) {
            StringBuilder sb = new StringBuilder("The following asserts failed:");
            boolean first = true;
            for (Map.Entry<AssertionError, IAssert<?>> ae : m_errors.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append("\n\t");
                sb.append(ae.getKey().getMessage());
                ae.getKey().printStackTrace();
            }
            m_errors.clear();
            throw new AssertionError(sb.toString());
        }
    }

    public void flush() {
        m_errors.clear();
    }
}
