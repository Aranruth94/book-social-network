package com.piotr.book_network.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${application.file.upload.photos-output-path}")
    private String fileUploadPath;

    @Override
    public String saveFile(@NonNull MultipartFile file,
                           @NonNull Integer userId) {
        final String fileUploadSubPath = "users" + File.separator + userId;
        return uploadFile(file, fileUploadSubPath);
    }

    private String uploadFile(@NonNull MultipartFile file,
                              @NonNull String fileUploadSubPath) {
        final String finalUploadPath = fileUploadPath + File.separator + fileUploadSubPath;
        if (!createDirectory(finalUploadPath)) {
            log.warn("Failed to create directory: {}", finalUploadPath);
            return null;
        }
        final String fileExtension = getFileExtension(file.getOriginalFilename());
        String targetFilePath = finalUploadPath + File.separator + System.currentTimeMillis() + fileExtension;
        if (writeFile(file, targetFilePath)) {
            log.info("File saved: {}", targetFilePath);
            return targetFilePath;
        }
        return null;
    }

    private boolean createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            return directory.mkdirs();
        }
        return true;
    }

    private String getFileExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return null;
        }
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return originalFilename.substring(lastDotIndex).toLowerCase();
    }

    private boolean writeFile(MultipartFile file, String targetFilePath) {
        Path targetPath = Paths.get(targetFilePath);
        try {
            Files.write(targetPath, file.getBytes());
            return true;
        } catch (IOException e) {
            log.error("Failed to save file: {}", targetFilePath, e);
            return false;
        }
    }
}