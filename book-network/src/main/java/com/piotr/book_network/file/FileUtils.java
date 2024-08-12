package com.piotr.book_network.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileUtils {

    public static byte[] readFileFromLocation(String fileUrl) {
        if (isFileUrlBlank(fileUrl)) {
            return null;
        }
        return readFileBytes(fileUrl);
    }

    private static boolean isFileUrlBlank(String fileUrl) {
        return StringUtils.isBlank(fileUrl);
    }

    private static byte[] readFileBytes(String fileUrl) {
        try {
            Path path = Path.of(fileUrl);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Error while reading file from location: {}", fileUrl, e);
            return null;
        }
    }
}