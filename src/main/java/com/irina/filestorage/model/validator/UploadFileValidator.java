package com.irina.filestorage.model.validator;

import com.irina.filestorage.config.FileStorageProps;
import com.irina.filestorage.model.UploadFileRequest;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@AllArgsConstructor
public class UploadFileValidator implements Validator {
    private static final String FILENAME_HAS_INCORRECT_SIZE = "Filename %s too long. It should be between 1 and 64 characters";
    private static final String FILENAME_CONTAINS_ILLEGAL_CHARACTERS = "Filename %s contains illegal characters." +
            " It should contain only a-z, A-Z, 0-9, -, _";
    private static final String FILE_TOO_LARGE = "File %s is too large. Maximum size allowed: %s MB";
    private static final String FILE_ALREADY_EXISTS = "A file with the same name (%s) already exists";
    private static final String FILE_NOT_FOUND = "No file with that name (%s) exists";

    private final FileStorageProps fileStorageProps;

    @Override
    public boolean supports(final Class<?> aClass) {
        return UploadFileRequest.class.equals(aClass);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        final UploadFileRequest uploadFileRequest = (UploadFileRequest) o;
        final String originalFilename = uploadFileRequest.getFile().getOriginalFilename();
        final boolean replaceFile = uploadFileRequest.getReplaceFile();
        if (!replaceFile) {
            checkFileName(originalFilename, errors);
        }
        checkFileExists(originalFilename, replaceFile, errors);
        checkFileSize(uploadFileRequest, errors);
    }

    private void checkFileName(final String fileName, final Errors errors) {
        final String fileNameWithoutExtension = FilenameUtils.removeExtension(fileName);
        if (fileNameWithoutExtension == null
                || fileNameWithoutExtension.length() < 1
                || fileNameWithoutExtension.length() > 64) {
            errors.reject(String.format(FILENAME_HAS_INCORRECT_SIZE, fileName));
        }
        if (fileNameWithoutExtension != null
                && !fileNameWithoutExtension.matches("[a-zA-Z0-9_\\-]{1,64}")) {
            errors.reject(String.format(FILENAME_CONTAINS_ILLEGAL_CHARACTERS, fileName));
        }
    }

    private void checkFileExists(final String fileName, final boolean replaceFile, final Errors errors) {
        final boolean fileExists = fileExists(fileName);

        if (fileExists && !replaceFile) {
            errors.reject(String.format(FILE_ALREADY_EXISTS, fileName));
        } else if (!fileExists && replaceFile) {
            errors.reject(String.format(FILE_NOT_FOUND, fileName));
        }
    }

    private void checkFileSize(final UploadFileRequest uploadFileRequest, final Errors errors) {
        if (uploadFileRequest.getFile().getSize() > fileStorageProps.getUploadMaxSize()) {
            errors.reject(String.format(FILE_TOO_LARGE,
                    uploadFileRequest.getFile().getOriginalFilename(),
                    fileStorageProps.getUploadMaxSize() / 1048576.0));
        }
    }

    private boolean fileExists(final String fileName) {
        return Files.exists(Paths.get(fileStorageProps.getBasePath(), fileName).normalize());
    }
}
