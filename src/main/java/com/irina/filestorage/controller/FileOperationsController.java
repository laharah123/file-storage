package com.irina.filestorage.controller;

import com.irina.filestorage.model.*;
import com.irina.filestorage.model.validator.FileSearchValidator;
import com.irina.filestorage.model.validator.UploadFileValidator;
import com.irina.filestorage.service.FileOperationsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
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
    private final FileSearchValidator fileSearchValidator;

    @InitBinder
    protected void initCreateFileBinder(final WebDataBinder binder) {
        binder.setValidator(uploadFileValidator);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity uploadFile(@Valid UploadFileRequest uploadFileRequest,
                                     BindingResult result) throws IOException {
        if (result.hasErrors()) {
            return buildErrorResponseEntity("File %s has an error: %s",
                    uploadFileRequest.getFile().getOriginalFilename(),
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
    public ResponseEntity<StorageSizeResponse> getSizeOfStorage() throws IOException {
        return ResponseEntity.ok(new StorageSizeResponse(fileOperationsService.getSizeOfStorage()));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity searchFiles(@RequestParam("search") final String fileNameRegex,
                                      @RequestParam(name = "pageSize", defaultValue = "${filestorage.file.pageSize}") final Integer pageSize,
                                      @RequestParam(name = "pageNumber", defaultValue = "1") final Integer pageNumber) throws IOException {
        final FileSearchRequest fileSearchRequest = new FileSearchRequest(fileNameRegex, pageSize, pageNumber);

        DataBinder dataBinder = new DataBinder(fileSearchRequest);
        dataBinder.setValidator(fileSearchValidator);
        dataBinder.validate();
        final BindingResult result = dataBinder.getBindingResult();

        if (result.hasErrors()) {
            return buildErrorResponseEntity("Search request %s has an error: %s",
                    fileSearchRequest.getFileNameRegex(),
                    result);
        } else {
            return ResponseEntity.ok(new FileSearchResponse(pageSize, pageNumber,
                    fileOperationsService.getMatchingFileNames(fileNameRegex, pageSize, pageNumber)));
        }
    }

    private ResponseEntity<ErrorResponse> buildErrorResponseEntity(final String messageTemplate,
                                                                   final String target,
                                                                   final BindingResult result) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        result.getAllErrors().stream()
                                .map(error -> {
                                    log.debug(String.format(messageTemplate, target, error.getCode()));
                                    return error.getCode();
                                })
                                .collect(Collectors.toList())
                ));
    }
}
