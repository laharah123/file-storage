package com.irina.filestorage.controller;

import com.irina.filestorage.model.ErrorResponse;
import com.irina.filestorage.model.SizeResponse;
import com.irina.filestorage.model.UploadFileRequest;
import com.irina.filestorage.model.validator.UploadFileValidator;
import com.irina.filestorage.service.FileOperationsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
@AllArgsConstructor
@Slf4j
public class FileOperationsController {
    private final UploadFileValidator uploadFileValidator;
    private final FileOperationsService fileOperationsService;


    @InitBinder
    protected void initCreateFileBinder(final WebDataBinder binder) {
        binder.setValidator(uploadFileValidator);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity uploadFile(@Valid UploadFileRequest uploadFileRequest,
                                     BindingResult result) throws IOException {
        if (result.hasErrors()) {
            return buildErrorResponseEntity(uploadFileRequest.getFile().getOriginalFilename(),
                    result);
        }
        fileOperationsService.uploadFile(uploadFileRequest.getFile());
        if (uploadFileRequest.getReplaceFile()) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.created(URI.create(String.format("/files/%s",
                    uploadFileRequest.getFile().getOriginalFilename()))).build();
        }
    }

    @GetMapping(value = "/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFile(@PathVariable("fileName") final String fileName) throws MalformedURLException {
        try {
            final Resource fileResource = fileOperationsService.downloadFile(fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(fileResource);
        } catch (final FileNotFoundException fileNotFoundException) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/{fileName}")
    public ResponseEntity<Void> deleteFile(@PathVariable("fileName") final String fileName) throws IOException {
        final boolean deletedFile = fileOperationsService.deleteFile(fileName);
        if (deletedFile) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/size", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SizeResponse> getSizeOfStorage() throws IOException {
        return ResponseEntity.ok(new SizeResponse(fileOperationsService.getSizeOfStorage()));
    }

    private ResponseEntity<ErrorResponse> buildErrorResponseEntity(final String fileName,
                                                                   final BindingResult result) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        result.getAllErrors().stream()
                                .map(error -> {
                                    log.debug(String.format("File %s has an error: %s", fileName, error.getCode()));
                                    return error.getCode();
                                })
                                .collect(Collectors.toList())
                ));
    }
}
