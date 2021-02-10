package com.irina.filestorage.model.validator;

import com.irina.filestorage.config.FileStorageProps;
import com.irina.filestorage.model.CreateFileRequest;
import com.irina.filestorage.model.validator.util.FileValidatorUtil;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@AllArgsConstructor
public class CreateFileValidator implements Validator {
    private final static String FILENAME_HAS_INCORRECT_SIZE = "Filename should be between 1 and 64 characters";
    private final static String FILENAME_CONTAINS_ILLEGAL_CHARACTERS = "Filename should contain only a-z, A-Z, 0-9, -, _";
    private final static String FILE_TOO_LARGE = "File is too large. Maximum size allowed: %s MB";
    private final static String FILE_ALREADY_EXISTS = "A file with the same name already exists";

    private final FileStorageProps fileStorageProps;

    @Override
    public boolean supports(final Class<?> aClass) {
        return CreateFileRequest.class.equals(aClass);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        final CreateFileRequest createFileRequest = (CreateFileRequest) o;
        checkFileName(createFileRequest.getFile().getOriginalFilename(), errors);
        checkFileSize(createFileRequest.getFile().getSize(), errors);
    }

    private void checkFileName(final String fileName, final Errors errors) {
        final String fileNameWithoutExtension = FilenameUtils.removeExtension(fileName);
        if (fileNameWithoutExtension == null
                || fileNameWithoutExtension.length() < 1
                || fileNameWithoutExtension.length() > 64) {
            errors.reject(FILENAME_HAS_INCORRECT_SIZE);
        }
        if (fileNameWithoutExtension != null
                && !fileNameWithoutExtension.matches("[a-zA-Z0-9_\\-]{1,64}")) {
            errors.reject(FILENAME_CONTAINS_ILLEGAL_CHARACTERS);
        }
        if (fileName != null
                && FileValidatorUtil.fileExists(fileStorageProps.getBasePath(), fileName)) {
            errors.reject(FILE_ALREADY_EXISTS);
        }
    }

    private void checkFileSize(final Long fileSize, final Errors errors) {
        if (fileSize > fileStorageProps.getUploadMaxSize()) {
            errors.reject(String.format(FILE_TOO_LARGE, fileStorageProps.getUploadMaxSize() / 1048576.0));
        }
    }
}
