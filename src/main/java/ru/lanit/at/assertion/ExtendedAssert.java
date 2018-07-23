package ru.lanit.at.assertion;

import io.qameta.allure.Attachment;
import io.qameta.allure.Flaky;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;
import org.testng.collections.Maps;
import ru.lanit.at.driver.DriverManager;

import java.util.Map;

public class ExtendedAssert extends SoftAssert {
    private static Logger log = LogManager.getLogger(ExtendedAssert.class.getSimpleName());
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
            log.debug("Успешно проверено: [{}]", a.getActual());
        } catch (AssertionError ex) {
            log.error(ex.getMessage());
            onAssertFailure(a, ex);
            m_errors.put(ex, a);
            attachErrorMsg(ex);
            driverManager.takeScreenshot();
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
            if (isCritical) sb.append(" [BLOCKER]");
            m_errors.clear();
            throw new AssertionError(sb.toString());
        }
    }

    public void flush() {
        m_errors.clear();
    }
}
