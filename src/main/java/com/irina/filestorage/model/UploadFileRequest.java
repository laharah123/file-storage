package com.irina.filestorage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadFileRequest {
    private MultipartFile file;
    private Boolean replaceFile = Boolean.FALSE;
}
