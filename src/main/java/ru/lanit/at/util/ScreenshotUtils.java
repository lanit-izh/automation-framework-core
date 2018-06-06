package ru.lanit.at.util;

import io.qameta.allure.Attachment;
import org.openqa.selenium.WebDriver;
import ru.lanit.at.exceptions.FrameworkRuntimeException;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ScreenshotUtils {

    @Attachment(value = "Page screenshot", type = "image/png")
    public static byte[] takeScreenshot(WebDriver webDriver) {
        AShot aShot = new AShot();
        BufferedImage bufferedImage = aShot.shootingStrategy(
                ShootingStrategies.viewportPasting(200)
        ).takeScreenshot(webDriver).getImage();
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
}
