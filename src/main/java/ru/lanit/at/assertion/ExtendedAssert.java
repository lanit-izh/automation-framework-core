package ru.lanit.at.assertion;

import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;
import org.testng.collections.Maps;
import ru.lanit.at.driver.DriverManager;
import ru.lanit.at.exceptions.FrameworkRuntimeException;
import ru.yandex.qatools.allure.annotations.Attachment;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
            takeScreenshot();
            if (isCritical) {
                this.assertAll();
            }
        } finally {
            this.isCritical = false;
            onAfterAssert(a);
        }
    }

    @Attachment(value = "Page screenshot", type = "image/png")
    private byte[] takeScreenshot() {
        AShot aShot = new AShot();
        BufferedImage bufferedImage = aShot.shootingStrategy(ShootingStrategies.viewportPasting(500)).takeScreenshot(driverManager.getDriver()).getImage();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
            byteArrayOutputStream.flush();
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return imageBytes;
        } catch (IOException e) {
            throw new FrameworkRuntimeException("Exception while taking screenshot.", e);
        }
    }

    @Attachment(value = "Error message")
    private String attachErrorMsg(AssertionError assertionError){
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
