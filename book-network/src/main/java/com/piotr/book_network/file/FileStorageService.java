package com.piotr.book_network.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String saveFile(MultipartFile file, Integer userId);
}
