package com.irina.filestorage.controller;

import com.irina.filestorage.model.ErrorResponse;
import com.irina.filestorage.model.FileUploadRequest;
import com.irina.filestorage.model.validator.FileUploadValidator;
import com.irina.filestorage.service.FileOperationsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/file")
@AllArgsConstructor
@Slf4j
public class FileOperationsController {
    private final FileUploadValidator fileUploadValidator;
    private final FileOperationsService fileOperationsService;

    @InitBinder
    protected void initFileUploadBinder(final WebDataBinder binder) {
        binder.setValidator(fileUploadValidator);
    }

    @PostMapping
    public ResponseEntity uploadFile(@Valid FileUploadRequest fileUploadRequest,
                                     BindingResult result) throws IOException {
        if (result.hasErrors()) {
            return buildErrorResponseEntity(fileUploadRequest.getFile().getOriginalFilename(),
                    result);
        }
        fileOperationsService.uploadFile(fileUploadRequest.getFile());
        return ResponseEntity.created(URI.create(String.format("/file/%s",
                fileUploadRequest.getFile().getOriginalFilename()))).build();
    }

    @GetMapping(value = "/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getFile(@PathVariable("fileName") String fileName) {
        return ResponseEntity.ok("Your file is " + fileName);
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
