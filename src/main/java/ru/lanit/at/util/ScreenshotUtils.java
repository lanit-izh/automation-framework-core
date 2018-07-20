package ru.lanit.at.util;

import io.qameta.allure.Attachment;
import org.openqa.selenium.WebDriver;
import ru.lanit.at.exceptions.FrameworkRuntimeException;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class ScreenshotUtils {

    @Attachment(value = "Page screenshot", type = "image/png")
    public static byte[] takeScreenshot(WebDriver webDriver) {
        if (isAlertPresented(webDriver)) {
            String alertText = webDriver.switchTo().alert().getText();
            saveTextLog(alertText, "Alert text");
            int closeAlertRetries = 0;
            while (isAlertPresented(webDriver)) {
                webDriver.switchTo().alert().dismiss();
                if (closeAlertRetries++ > 10) {
                    throw new FrameworkRuntimeException("Не удалось сделать скриншот из-за алерта \"" + alertText + "\"");
                }
            }
        }
        try {
            BufferedImage bufferedImage = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(10))
                    .takeScreenshot(webDriver)
                    .getImage();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);

            byteArrayOutputStream.flush();
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return imageBytes;

        } catch (Exception e) {
            saveTextLog(e.toString(), "Ошибка при снятии скриншота");
            return new byte[]{};
        }
    }


    // 10 раз пытается переключиться на аллерт, так как не всегда срабатывает с 1 раза
    private static boolean isAlertPresented(WebDriver driver) {
        for (int i = 0; i < 10; i++) {
            try {
                driver.switchTo().alert();
                return true;
            } catch (Exception ignore) {
            }
        }
        return false;
    }

    @Attachment(value = "{1}", type = "text/plain")
    private static String saveTextLog(String message, String caption) {
        return message;
    }
}
