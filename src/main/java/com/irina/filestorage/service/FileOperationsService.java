package com.irina.filestorage.service;

import com.irina.filestorage.config.FileStorageProps;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@AllArgsConstructor
public class FileOperationsService {
    private final FileStorageProps fileStorageProps;

    public void uploadFile(final MultipartFile multipartFile) throws IOException {
        final File file = new File(fileStorageProps.getBasePath(),
                multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);
    }
}
