package ru.lanit.at.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.lanit.at.driver.DriverManager;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ScreenShooter {
    private static final Logger LOGGER = LogManager.getLogger(ScreenShooter.class.getName());
    private DriverManager driverManager;

    public void setDriverManager(DriverManager driverManager) {
        this.driverManager = driverManager;
    }


    public byte[] takeScreenshot() {
        return takeScreenshot(ShootingStrategies.viewportPasting(10));
    }

    public byte[] takeScreenshot(ShootingStrategy strategies) {
        if (driverManager.isActive()) {
            try {
                LOGGER.info("Выполнение скриншота");
                BufferedImage bufferedImage = new AShot().shootingStrategy(strategies)
                        .takeScreenshot(driverManager.getDriver())
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
        LOGGER.error("Драйвер не активен выполнение скриншота не возможно");
        return new byte[]{};
    }


}
