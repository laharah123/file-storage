package com.irina.filestorage.service;

import com.irina.filestorage.config.FileStorageProps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
@Slf4j
public class FileOperationsService {

    private final FileStorageProps fileStorageProps;

    public void uploadFile(final MultipartFile multipartFile) throws IOException {
        final Path filePath = Paths.get(fileStorageProps.getBasePath(), multipartFile.getOriginalFilename()).normalize();
        multipartFile.transferTo(filePath);
    }

    public Resource downloadFile(final String fileName) throws MalformedURLException, FileNotFoundException {
        final Path filePath = Paths.get(fileStorageProps.getBasePath(), fileName).normalize();
        final Resource fileResource = new UrlResource(filePath.toUri());
        if (!fileResource.exists()) {
            log.debug(String.format("File %s not found", fileName));
            throw new FileNotFoundException();
        }
        return fileResource;
    }

    public boolean deleteFile(final String fileName) throws IOException {
        final Path filePath = Paths.get(fileStorageProps.getBasePath(), fileName).normalize();
        return Files.deleteIfExists(filePath);
    }

    public long getSizeOfStorage() throws IOException {
        final Path baseDirPath = Paths.get(fileStorageProps.getBasePath());
        try (Stream<Path> files = Files.walk(baseDirPath, 1)) {
            return files.count() - 1;
        }
    }
}
