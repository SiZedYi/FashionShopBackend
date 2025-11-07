package com.fashion.leon.fashionshopbackend.service;

import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Override
    public void init() {

    }

    @Override
    public void init(String pathname) {
        try {
            Path dir = Paths.get(pathname);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    @Override
    public String save(MultipartFile file, String pathname) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            Path dir = Paths.get(pathname);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Files.copy(file.getInputStream(), dir.resolve(newFilename));
            // Return the path relative to the base directory
            String normalizedPath = pathname.replace("\\", "/");
            if (!normalizedPath.startsWith("/")) normalizedPath = "/" + normalizedPath;
            if (!normalizedPath.endsWith("/")) normalizedPath += "/";
            return normalizedPath + newFilename;
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    @Override
    public Path load(String filename, String pathname) {
        return Paths.get(pathname).resolve(filename);
    }

    @Override
    public void delete(String filename, String pathname) throws IOException {
        Path file = load(filename, pathname);
        FileSystemUtils.deleteRecursively(file);
    }
}
