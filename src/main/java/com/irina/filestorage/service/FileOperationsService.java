package com.irina.filestorage.service;

import com.irina.filestorage.config.FileStorageProps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
@Slf4j
public class FileOperationsService {

    private final FileStorageProps fileStorageProps;

    public void uploadFile(final MultipartFile multipartFile) throws IOException {
        final File file = new File(fileStorageProps.getBasePath(),
                multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);
    }

    public Resource downloadFile(final String fileName) throws MalformedURLException, FileNotFoundException {
        final Path path = Paths.get(fileStorageProps.getBasePath(), fileName).normalize();
        final Resource fileResource = new UrlResource(path.toUri());
        if (!fileResource.exists()) {
            log.debug(String.format("File %s not found", fileName));
            throw new FileNotFoundException();
        }
        return fileResource;
    }
}
