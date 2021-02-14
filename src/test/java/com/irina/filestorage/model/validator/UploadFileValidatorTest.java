package com.irina.filestorage.model.validator;

import com.irina.filestorage.config.FileStorageProps;
import com.irina.filestorage.model.UploadFileRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadFileValidatorTest {
    private final static MockedStatic<Files> staticMockFiles = Mockito.mockStatic(Files.class);

    private UploadFileValidator uploadFileValidator;

    @Mock
    private Errors errors;
    @Mock
    private MultipartFile multipartFile;

    @BeforeEach
    void setup() {
        uploadFileValidator = new UploadFileValidator(buildFileStorageProps());
        staticMockFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);
    }

    @AfterAll
    static void tearDown() {
        staticMockFiles.close();
    }

    @Test
    void testSupportsWithCorrectClassSuccess() {
        final UploadFileRequest uploadFileRequest = new UploadFileRequest();
        assertTrue(uploadFileValidator.supports(uploadFileRequest.getClass()));
    }

    @Test
    void testSupportsWithIncorrectClassFail() {
        final Object object = new Object();
        assertFalse(uploadFileValidator.supports(object.getClass()));
    }

    @Test
    void testValidateWithCorrectDataSuccess() {
        final UploadFileRequest uploadFileRequest = buildUploadFileRequest(false);
        when(multipartFile.getSize()).thenReturn(20L);
        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
        uploadFileValidator.validate(uploadFileRequest, errors);
        verifyNoInteractions(errors);
    }

    @Test
    void testValidateWithFileNameTooShortFail() {
        final UploadFileRequest uploadFileRequest = buildUploadFileRequest(false);
        final ArgumentCaptor<String> errorMessageArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(errors).reject(anyString(), errorMessageArgumentCaptor.capture());
        when(multipartFile.getOriginalFilename()).thenReturn(".txt");
        when(multipartFile.getSize()).thenReturn(20L);
        uploadFileValidator.validate(uploadFileRequest, errors);
        verify(errors, times(1)).reject(anyString(), anyString());
        assertEquals("File name .txt is incorrect. It should be between 1 and 64 characters long" +
                        " and it should contain only a-z, A-Z, 0-9, -, _",
                errorMessageArgumentCaptor.getValue());
    }

    @Test
    void testValidateWithFileNameTooLongFail() {
        final UploadFileRequest uploadFileRequest = buildUploadFileRequest(false);
        final ArgumentCaptor<String> errorMessageArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(errors).reject(anyString(), errorMessageArgumentCaptor.capture());
        when(multipartFile.getOriginalFilename()).thenReturn("thisIsAVeryVeryLongFileNameOver64CharactersWhichIsTheMaximumAllowed.txt");
        when(multipartFile.getSize()).thenReturn(20L);
        uploadFileValidator.validate(uploadFileRequest, errors);
        verify(errors, times(1)).reject(anyString(), anyString());
        assertEquals("File name thisIsAVeryVeryLongFileNameOver64CharactersWhichIsTheMaximumAllowed.txt is incorrect." +
                        " It should be between 1 and 64 characters long" +
                        " and it should contain only a-z, A-Z, 0-9, -, _",
                errorMessageArgumentCaptor.getValue());
    }

    @Test
    void testValidateWithFileNameInvalidCharactersFail() {
        final UploadFileRequest uploadFileRequest = buildUploadFileRequest(false);
        final ArgumentCaptor<String> errorMessageArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(errors).reject(anyString(), errorMessageArgumentCaptor.capture());
        when(multipartFile.getOriginalFilename()).thenReturn("This file contains illegal & characters.txt");
        when(multipartFile.getSize()).thenReturn(20L);
        uploadFileValidator.validate(uploadFileRequest, errors);
        verify(errors, times(1)).reject(anyString(), anyString());
        assertEquals("File name This file contains illegal & characters.txt is incorrect. It should be between 1 and 64 characters long" +
                        " and it should contain only a-z, A-Z, 0-9, -, _",
                errorMessageArgumentCaptor.getValue());
    }

    @Test
    void testValidateWithFileTooLargeFail() {
        final UploadFileRequest uploadFileRequest = buildUploadFileRequest(false);
        final ArgumentCaptor<String> errorMessageArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(errors).reject(anyString(), errorMessageArgumentCaptor.capture());
        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
        when(multipartFile.getSize()).thenReturn(1048580L);
        uploadFileValidator.validate(uploadFileRequest, errors);
        verify(errors, times(1)).reject(anyString(), anyString());
        assertEquals("File test.txt is too large. Maximum size allowed: 1.0 MB",
                errorMessageArgumentCaptor.getValue());
    }

    @Test
    void testValidateWithReplaceNonExistingFileFail() {
        final UploadFileRequest uploadFileRequest = buildUploadFileRequest(true);
        final ArgumentCaptor<String> errorMessageArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(errors).reject(anyString(), errorMessageArgumentCaptor.capture());
        when(multipartFile.getSize()).thenReturn(20L);
        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
        uploadFileValidator.validate(uploadFileRequest, errors);
        verify(errors, times(1)).reject(anyString(), anyString());
        assertEquals("No file with that name (test.txt) exists",
                errorMessageArgumentCaptor.getValue());
    }

    @Test
    void testValidateWithCreateExistingFileFail() {
        final UploadFileRequest uploadFileRequest = buildUploadFileRequest(false);
        final ArgumentCaptor<String> errorMessageArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(errors).reject(anyString(), errorMessageArgumentCaptor.capture());
        when(multipartFile.getSize()).thenReturn(20L);
        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
        staticMockFiles.when(() -> Files.exists(any(Path.class))).thenReturn(true);
        uploadFileValidator.validate(uploadFileRequest, errors);
        verify(errors, times(1)).reject(anyString(), anyString());
        assertEquals("A file with the same name (test.txt) already exists",
                errorMessageArgumentCaptor.getValue());
    }

    private UploadFileRequest buildUploadFileRequest(final boolean replaceFile) {
        final UploadFileRequest uploadFileRequest = new UploadFileRequest();
        uploadFileRequest.setFile(multipartFile);
        uploadFileRequest.setReplaceFile(replaceFile);
        return uploadFileRequest;
    }

    private FileStorageProps buildFileStorageProps() {
        final FileStorageProps fileStorageProps = new FileStorageProps();
        fileStorageProps.setPageSize(20L);
        fileStorageProps.setUploadMaxSize(1048576L);
        fileStorageProps.setBasePath("E:/test");
        return fileStorageProps;
    }
}