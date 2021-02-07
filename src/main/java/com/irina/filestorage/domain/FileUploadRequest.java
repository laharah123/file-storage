package com.irina.filestorage.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class FileUploadRequest {
    private MultipartFile file;
}
