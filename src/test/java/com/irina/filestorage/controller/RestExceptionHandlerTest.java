package com.irina.filestorage.controller;

import com.irina.filestorage.model.ErrorResponse;
import com.irina.filestorage.model.validator.FileOperationValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {
    private final RestExceptionHandler restExceptionHandler = new RestExceptionHandler();

    @Mock
    private WebRequest webRequest;

    @Test
    void testHandleUnknownExceptionsSuccess() {
        ResponseEntity<Object> responseEntity = restExceptionHandler
                .handleUnknownExceptions(new IOException(), webRequest);

        assertEquals("An error has occurred. Please try again later or contact the service desk.",
                responseEntity.getBody().toString());
    }

    @Test
    void testHandleValidationExceptionSuccess() {
        ResponseEntity<ErrorResponse> responseEntity = restExceptionHandler
                .handleValidationException(buildFileOperationValidationException(), webRequest);
        assertEquals(3, responseEntity.getBody().getErrorMessages().size());
        assertEquals("This is the first error", responseEntity.getBody().getErrorMessages().get(0));
        assertEquals("This is the second error", responseEntity.getBody().getErrorMessages().get(1));
        assertEquals("This is the third error", responseEntity.getBody().getErrorMessages().get(2));
    }

    private FileOperationValidationException buildFileOperationValidationException() {
        final ObjectError error1 = new ObjectError("test", "This is the first error");
        final ObjectError error2 = new ObjectError("test", "This is the second error");
        final ObjectError error3 = new ObjectError("test", "This is the third error");
        return new FileOperationValidationException(Arrays.asList(error1, error2, error3));
    }
}