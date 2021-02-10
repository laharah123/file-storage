package com.irina.filestorage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class ReplaceFileRequest {
    private MultipartFile file;
}
