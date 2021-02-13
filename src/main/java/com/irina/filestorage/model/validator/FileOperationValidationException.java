package com.irina.filestorage.model.validator;

import lombok.Getter;
import org.springframework.validation.ObjectError;

import java.util.List;

@Getter
public class FileOperationValidationException extends RuntimeException {
    private final List<ObjectError> errors;

    public FileOperationValidationException(final List<ObjectError> errors) {
        this.errors = errors;
    }
}
