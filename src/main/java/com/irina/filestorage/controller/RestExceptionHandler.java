package com.irina.filestorage.controller;

import com.irina.filestorage.model.ErrorResponse;
import com.irina.filestorage.model.validator.FileOperationValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String UNKNOWN_EXCEPTION
            = "An error has occurred. Please try again later or contact the service desk.";

    @ExceptionHandler(value = FileOperationValidationException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(
            final FileOperationValidationException ex, final WebRequest request) {
        return buildErrorResponseEntity(ex.getErrors());
    }

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<Object> handleUnknownExceptions(
            final Exception ex, final WebRequest request) {
        log.error(ex.getMessage(), ex);
        return handleExceptionInternal(ex, UNKNOWN_EXCEPTION,
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponseEntity(final List<ObjectError> errors) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        errors.stream()
                                .map(error -> {
                                    log.debug(error.getCode());
                                    return error.getCode();
                                })
                                .collect(Collectors.toList())
                ));
    }
}
