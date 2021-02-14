package com.irina.filestorage.model.validator;

import com.irina.filestorage.model.FileSearchRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileSearchValidatorTest {
    private final FileSearchValidator fileSearchValidator = new FileSearchValidator();

    @Mock
    private Errors errors;

    @Test
    void testSupportsWithCorrectClassSuccess() {
        final FileSearchRequest fileSearchRequest = new FileSearchRequest();
        assertTrue(fileSearchValidator.supports(fileSearchRequest.getClass()));
    }

    @Test
    void testSupportsWithIncorrectClassFail() {
        final Object object = new Object();
        assertFalse(fileSearchValidator.supports(object.getClass()));
    }

    @Test
    void testValidateWithCorrectDataSuccess() {
        final FileSearchRequest fileSearchRequest = new FileSearchRequest("^.*[a-z]+def\\.$", 2, 3);
        fileSearchValidator.validate(fileSearchRequest, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void testValidateWithIncorrectPageSizeFail() {
        final FileSearchRequest fileSearchRequest = new FileSearchRequest("^.*[a-z]+def\\.$", 0, 3);
        final ArgumentCaptor<String> errorMessageArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(errors).reject(anyString(), errorMessageArgumentCaptor.capture());
        fileSearchValidator.validate(fileSearchRequest, errors);
        verify(errors, times(1)).reject(anyString(), anyString());
        assertEquals("Page size 0 is invalid. It must be a positive integer number",
                errorMessageArgumentCaptor.getValue());
    }

    @Test
    void testValidateWithIncorrectPageNumberFail() {
        final FileSearchRequest fileSearchRequest = new FileSearchRequest("^.*[a-z]+def\\.$", 1, 0);
        final ArgumentCaptor<String> errorMessageArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(errors).reject(anyString(), errorMessageArgumentCaptor.capture());
        fileSearchValidator.validate(fileSearchRequest, errors);
        verify(errors, times(1)).reject(anyString(), anyString());
        assertEquals("Page number 0 is invalid. It must be a positive integer number",
                errorMessageArgumentCaptor.getValue());
    }

    @Test
    void testValidateWithInvalidSearchPatternFail() {
        final FileSearchRequest fileSearchRequest = new FileSearchRequest("^&\\.+><[$", 1, 3);
        final ArgumentCaptor<String> errorMessageArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(errors).reject(anyString(), errorMessageArgumentCaptor.capture());
        fileSearchValidator.validate(fileSearchRequest, errors);
        verify(errors, times(1)).reject(anyString(), anyString());
        assertEquals("The search term ^&\\.+><[$ is not a valid regex",
                errorMessageArgumentCaptor.getValue());
    }
}