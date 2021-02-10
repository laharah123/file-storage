package com.irina.filestorage.controller;

import com.irina.filestorage.model.CreateFileRequest;
import com.irina.filestorage.model.ErrorResponse;
import com.irina.filestorage.model.validator.CreateFileValidator;
import com.irina.filestorage.model.validator.ReplaceFileValidator;
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
    private final CreateFileValidator createFileValidator;
    private final ReplaceFileValidator replaceFileValidator;
    private final FileOperationsService fileOperationsService;


    @InitBinder(value = "createFileValidator")
    protected void initCreateFileBinder(final WebDataBinder binder) {
        binder.setValidator(createFileValidator);
    }

    @InitBinder(value = "replaceFileValidator")
    protected void initReplaceFileBinder(final WebDataBinder binder) {
        binder.setValidator(replaceFileValidator);
    }

    @PostMapping
    public ResponseEntity createFile(@Valid CreateFileRequest createFileRequest,
                                     BindingResult result) throws IOException {
        if (result.hasErrors()) {
            return buildErrorResponseEntity(createFileRequest.getFile().getOriginalFilename(),
                    result);
        }
        fileOperationsService.createFile(createFileRequest.getFile());
        return ResponseEntity.created(URI.create(String.format("/files/%s",
                createFileRequest.getFile().getOriginalFilename()))).build();
    }

    @GetMapping(value = "/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFile(@PathVariable("fileName") String fileName) throws MalformedURLException {
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

    @PutMapping(value = "/{fileName}")
    public ResponseEntity<Resource> replaceFile(@PathVariable("fileName") String fileName,
                                                @Valid ReplaceFileValidator replaceFileValidator,
                                                BindingResult result) {
        return ResponseEntity.ok().build();
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
