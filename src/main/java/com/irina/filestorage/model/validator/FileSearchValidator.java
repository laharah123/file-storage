package com.irina.filestorage.model.validator;

import com.irina.filestorage.model.FileSearchRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
public class FileSearchValidator implements Validator {
    private final static String INCORRECT_PAGE_SIZE = "Page size must be a positive integer number";
    private final static String INCORRECT_PAGE_NUMBER = "Page number must be a positive integer number";
    private final static String INVALID_SEARCH_PATTERN = "The search term must be a valid regex";

    @Override
    public boolean supports(final Class<?> clazz) {
        return FileSearchRequest.class.equals(clazz);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        final FileSearchRequest fileSearchRequest = (FileSearchRequest) target;
        checkPageSizeAndNumber(fileSearchRequest.getPageSize(), fileSearchRequest.getPageNumber(), errors);
        checkSearchRegex(fileSearchRequest.getFileNameRegex(), errors);
    }

    private void checkPageSizeAndNumber(final Integer pageSize, final Integer pageNumber, final Errors errors) {
        if (pageSize < 1) {
            errors.reject(INCORRECT_PAGE_SIZE);
        }
        if (pageNumber < 1) {
            errors.reject(INCORRECT_PAGE_NUMBER);
        }
    }

    private void checkSearchRegex(final String fileNameRegex, final Errors errors) {
        try {
            Pattern.compile(fileNameRegex);
        } catch (PatternSyntaxException exception) {
            errors.reject(INVALID_SEARCH_PATTERN);
        }
    }
}
