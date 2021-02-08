package com.irina.filestorage.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String UNKNOWN_EXCEPTION
            = "An error has occurred. Please try again later or contact the service desk.";

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<Object> handleUnknownExceptions(
            Exception ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        return handleExceptionInternal(ex, UNKNOWN_EXCEPTION,
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
