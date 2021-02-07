package com.irina.filestorage.domain.validator;

import com.irina.filestorage.domain.FileUploadRequest;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@Setter
public class FileUploadValidator implements Validator {
    private final static String FILENAME_HAS_INCORRECT_SIZE = "Filename should be between 1 and 64 characters";
    private final static String FILENAME_CONTAINS_ILLEGAL_CHARACTERS = "Filename should contain only a-z, A-Z, 0-9, -, _";
    private final static String FILE_TOO_LARGE = "File is too large. Maximum size allowed: %s MB";
    private final static String FILE_ALREADY_EXISTS = "A file with the same name already exists";

    @Value("${filestorage.file.upload.maxSize}")
    private long maxSize;

    @Value("${filestorage.file.basePath}")
    private String basePath;

    @Override
    public boolean supports(final Class<?> aClass) {
        return FileUploadRequest.class.equals(aClass);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        final FileUploadRequest fileUploadRequest = (FileUploadRequest) o;
        checkFileName(fileUploadRequest.getFile().getOriginalFilename(), errors);
        checkFileSize(fileUploadRequest.getFile().getSize(), errors);
    }

    private void checkFileName(final String fileName, final Errors errors) {
        if (fileName == null || fileName.length() < 1 || fileName.length() > 64) {
            errors.reject(FILENAME_HAS_INCORRECT_SIZE);
        }
        if (fileName != null && !fileName.matches("[a-zA-Z0-9_\\-]{1,64}")) {
            errors.reject(FILENAME_CONTAINS_ILLEGAL_CHARACTERS);
        }
        if (fileName != null && fileAlreadyExists(fileName)) {
            errors.reject(FILE_ALREADY_EXISTS);
        }
    }

    private void checkFileSize(final Long fileSize, final Errors errors) {
        if (fileSize > maxSize) {
            errors.reject(FILE_TOO_LARGE);
        }
    }

    private boolean fileAlreadyExists(final String fileName) {
        return Files.exists(Paths.get(basePath, fileName));
    }
}
