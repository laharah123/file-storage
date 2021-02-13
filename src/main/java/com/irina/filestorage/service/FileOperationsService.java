package com.irina.filestorage.service;

import com.irina.filestorage.config.FileStorageProps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
@Slf4j
public class FileOperationsService {

    private final FileStorageProps fileStorageProps;

    public void uploadFile(final MultipartFile multipartFile) throws IOException {
        final Path filePath = Paths.get(fileStorageProps.getBasePath(), multipartFile.getOriginalFilename()).normalize();
        multipartFile.transferTo(filePath);
    }

    public Resource downloadFile(final String fileName) throws MalformedURLException, FileNotFoundException {
        final Path filePath = Paths.get(fileStorageProps.getBasePath(), fileName).normalize();
        final Resource fileResource = new UrlResource(filePath.toUri());
        if (!fileResource.exists()) {
            log.debug(String.format("File %s not found", fileName));
            throw new FileNotFoundException();
        }
        return fileResource;
    }

    public boolean deleteFile(final String fileName) throws IOException {
        final Path filePath = Paths.get(fileStorageProps.getBasePath(), fileName).normalize();
        return Files.deleteIfExists(filePath);
    }

    public long getSizeOfStorage() throws IOException {
        final Path baseDirPath = Paths.get(fileStorageProps.getBasePath()).normalize();

        try (final DirectoryStream<Path> files = Files.newDirectoryStream(baseDirPath)) {
            return StreamSupport.stream(files.spliterator(), false).count();
        }
    }

    public List<String> getMatchingFileNames(final String fileNameRegex, final int pageSize, final int pageNumber) throws IOException {
        final Path baseDirPath = Paths.get(fileStorageProps.getBasePath()).normalize();
        final PathMatcher regexFileNameMatcher = FileSystems.getDefault().getPathMatcher("regex:" + fileNameRegex);

        try (final Stream<Path> filePaths = Files.walk(baseDirPath, 1)) {
            return filePaths.filter(filePath -> regexFileNameMatcher.matches(filePath.getFileName()) && Files.isRegularFile(filePath))
                    .skip((long) (pageNumber - 1) * pageSize)
                    .limit(pageSize)
                    .map(filePath -> filePath.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        }
    }
}
