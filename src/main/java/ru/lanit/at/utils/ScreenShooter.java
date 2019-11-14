package ru.lanit.at.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ScreenShooter {
    private static final Logger LOGGER = LogManager.getLogger(ScreenShooter.class.getName());
    private WebDriver driver;

    public ScreenShooter(WebDriver driver) {
        this.driver = driver;
    }

    public byte[] takeScreenshot() {
        return takeScreenshot(ShootingStrategies.viewportPasting(10));
    }

    public byte[] takeScreenshot(ShootingStrategy strategies) {
        try {
            LOGGER.info("Выполнение скриншота");
            BufferedImage bufferedImage = new AShot().shootingStrategy(strategies)
                    .takeScreenshot(driver)
                    .getImage();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
            byteArrayOutputStream.flush();
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return imageBytes;
        } catch (IOException e) {
            LOGGER.error("Ошибка при снятии скриншота");
            return new byte[]{};
        }
    }


}
