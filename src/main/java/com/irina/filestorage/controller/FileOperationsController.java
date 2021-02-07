package com.irina.filestorage.controller;

import com.irina.filestorage.domain.FileUploadRequest;
import com.irina.filestorage.domain.validator.FileUploadValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileOperationsController {
    private FileUploadValidator fileUploadValidator;

    @InitBinder("fileUploadBinder")
    protected void initContainerTypeBinder(final WebDataBinder binder) {
        binder.setValidator(fileUploadValidator);
    }

    @PostMapping("/upload")
    public ResponseEntity uploadFile(@Valid FileUploadRequest fileUploadRequest,
                                     BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.created(URI.create(String.format("/files/%s",
                fileUploadRequest.getFile().getOriginalFilename()))).build();
    }

    @GetMapping(value = "/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getFile(@PathVariable("fileName") String fileName) {
        return ResponseEntity.ok("Your file is " + fileName);
    }
}
