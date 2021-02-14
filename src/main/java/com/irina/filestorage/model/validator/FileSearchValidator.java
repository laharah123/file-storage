package com.irina.filestorage.model.validator;

import com.irina.filestorage.model.FileSearchRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
public class FileSearchValidator implements Validator {
    private static final String INCORRECT_PAGE_SIZE_CODE = "filestorage.files.search.incorrectPageSize";
    private static final String INCORRECT_PAGE_SIZE = "Page size %s is invalid. It must be a positive integer number";
    private static final String INCORRECT_PAGE_NUMBER_CODE = "filestorage.files.search.incorrectPageNumber";
    private static final String INCORRECT_PAGE_NUMBER = "Page number %s is invalid. It must be a positive integer number";
    private static final String INVALID_SEARCH_PATTERN_CODE = "filestorage.files.search.invalidSearchPattern";
    private static final String INVALID_SEARCH_PATTERN = "The search term %s is not a valid regex";

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
            errors.reject(INCORRECT_PAGE_SIZE_CODE, String.format(INCORRECT_PAGE_SIZE, pageSize));
        }
        if (pageNumber < 1) {
            errors.reject(INCORRECT_PAGE_NUMBER_CODE, String.format(INCORRECT_PAGE_NUMBER, pageNumber));
        }
    }

    private void checkSearchRegex(final String fileNameRegex, final Errors errors) {
        try {
            Pattern.compile(fileNameRegex);
        } catch (PatternSyntaxException exception) {
            errors.reject(INVALID_SEARCH_PATTERN_CODE, String.format(INVALID_SEARCH_PATTERN, fileNameRegex));
        }
    }
}
