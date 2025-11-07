package com.fashion.leon.fashionshopbackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {
    void init();

    void init(String pathname);

    String save(MultipartFile file, String pathname);

    Path load(String filename, String pathname);

    void delete(String filename, String pathname) throws IOException;
}
