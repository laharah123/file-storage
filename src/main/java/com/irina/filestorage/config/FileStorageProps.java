package com.irina.filestorage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "filestorage.file")
@Data
public class FileStorageProps {
    private Long uploadMaxSize;

    private String basePath;
}
