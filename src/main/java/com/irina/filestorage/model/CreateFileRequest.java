package com.irina.filestorage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class CreateFileRequest {
    private MultipartFile file;
}
