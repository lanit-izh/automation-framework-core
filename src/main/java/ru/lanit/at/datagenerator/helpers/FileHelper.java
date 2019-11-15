package ru.lanit.at.datagenerator.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FileHelper {
    private static final Logger LOGGER = LogManager.getLogger(FileHelper.class.getName());

    public File generate(double fileSizeInMB, String extension) {
        File f = null;
        try {
            f = File.createTempFile("fileGen_", extension.startsWith(".") ? extension : "." + extension);
            byte[] data = generateData(fileSizeInMB * 1_048_576, extension);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
            bos.write(data);
            bos.close();
            LOGGER.info("Файл {} успешно сохранён на диск", f.getName());
        } catch (IOException var3) {
            var3.printStackTrace();
        }
        return f;
    }

    private byte[] generateData(double i, String extension) {
        byte[] generatedData = new byte[(int) i];
        Arrays.fill(generatedData, (byte) 0);
        LOGGER.info("Сгенерирован массив данных {} байт", generatedData.length);
        addExtensionSign(generatedData, getExtensionSign(extension));
        return generatedData;
    }

    private void addExtensionSign(byte[] generatedData, byte[] extensionSign) {
        for (int i = 0; i < Math.min(extensionSign.length, generatedData.length); i++) {
            generatedData[i] = extensionSign[i];
        }
    }

    private byte[] getExtensionSign(String extension) {
        String ext = extension.toUpperCase().replace(".", "");
        switch (ext) {
            case "RAR":
                return "Rar!".getBytes();
            case "BMP":
                return "BM".getBytes();
            case "PNG":
                return "‰PNG".getBytes();
            case "JPEG":
            case "JPE":
            case "JPG":
                return "яШя".getBytes();
            case "PDF":
                return "%PDF-1.5\n".getBytes();
            case "XLS":
            case "DOC":
                return "РП\u0011аЎ±\u001Aб ".getBytes();
            case "XLSX":
            case "DOCX": // это не ошибка, он действительно как зип-файл
            case "ZIP":
                return "PK".getBytes();
            default:
                return "Generated test file".getBytes();
        }
    }
}
