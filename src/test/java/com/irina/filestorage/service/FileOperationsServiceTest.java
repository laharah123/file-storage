package com.irina.filestorage.service;

import com.irina.filestorage.config.FileStorageProps;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileOperationsServiceTest {
    private final static MockedStatic<Paths> staticMockPaths = mockStatic(Paths.class);
    private final static MockedStatic<Files> staticMockFiles = mockStatic(Files.class);
    private final static MockedStatic<FileSystems> staticMockFileSystems = mockStatic(FileSystems.class);


    private FileOperationsService fileOperationsService;

    @Mock
    private Path filePath1;

    @Mock
    private Path filePath2;

    @Mock
    private Path filePath3;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private PathMatcher pathMatcher;

    @BeforeEach
    void setup() {
        fileOperationsService = new FileOperationsService(buildFileStorageProps());
        staticMockPaths.when(() -> Paths.get(anyString())).thenReturn(filePath1);
        staticMockFiles.when(() -> Files.walk(any(Path.class), eq(1))).thenReturn(buildStreamPaths());
        staticMockFiles.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
        staticMockFileSystems.when(FileSystems::getDefault).thenReturn(fileSystem);
        when(filePath1.normalize()).thenReturn(filePath1);
        when(fileSystem.getPathMatcher("regex:.*\\.pdf")).thenReturn(pathMatcher);
    }

    @AfterAll
    static void tearDown() {
        staticMockFiles.close();
        staticMockFileSystems.close();
        staticMockPaths.close();
    }

    @Test
    void testGetMatchingFileNamesOneMatchSuccess() throws IOException {
        when(pathMatcher.matches(filePath1)).thenReturn(true);
        when(pathMatcher.matches(filePath2)).thenReturn(false);
        when(pathMatcher.matches(filePath3)).thenReturn(false);
        when(filePath1.getFileName()).thenReturn(filePath1);
        when(filePath2.getFileName()).thenReturn(filePath2);
        when(filePath3.getFileName()).thenReturn(filePath3);
        when(filePath1.toString()).thenReturn("test.pdf");
        final List<String> result = fileOperationsService.getMatchingFileNames(".*\\.pdf", 3, 1);
        assertEquals(1, result.size());
        assertEquals("test.pdf", result.get(0));
    }

    @Test
    void testGetMatchingFileNamesAllMatchSuccess() throws IOException {
        when(pathMatcher.matches(filePath1)).thenReturn(true);
        when(pathMatcher.matches(filePath2)).thenReturn(true);
        when(pathMatcher.matches(filePath3)).thenReturn(true);
        when(filePath1.getFileName()).thenReturn(filePath1);
        when(filePath2.getFileName()).thenReturn(filePath2);
        when(filePath3.getFileName()).thenReturn(filePath3);
        when(filePath1.toString()).thenReturn("test.pdf");
        when(filePath2.toString()).thenReturn("test2.pdf");
        when(filePath3.toString()).thenReturn("test3.pdf");
        final List<String> result = fileOperationsService.getMatchingFileNames(".*\\.pdf", 3, 1);
        assertEquals(3, result.size());
        assertEquals("test.pdf", result.get(0));
        assertEquals("test2.pdf", result.get(1));
        assertEquals("test3.pdf", result.get(2));
    }

    @Test
    void testGetMatchingFileNamesAllMatchLimit1FirstPageSuccess() throws IOException {
        when(pathMatcher.matches(filePath1)).thenReturn(true);
        when(filePath1.getFileName()).thenReturn(filePath1);
        when(filePath1.toString()).thenReturn("test.pdf");
        final List<String> result = fileOperationsService.getMatchingFileNames(".*\\.pdf", 1, 1);
        assertEquals(1, result.size());
        assertEquals("test.pdf", result.get(0));
    }

    @Test
    void testGetMatchingFileNamesAllMatchLimit1SecondPageSuccess() throws IOException {
        when(pathMatcher.matches(filePath1)).thenReturn(true);
        when(pathMatcher.matches(filePath2)).thenReturn(true);
        when(filePath1.getFileName()).thenReturn(filePath1);
        when(filePath2.getFileName()).thenReturn(filePath2);
        when(filePath2.toString()).thenReturn("test2.pdf");
        final List<String> result = fileOperationsService.getMatchingFileNames(".*\\.pdf", 1, 2);
        assertEquals(1, result.size());
        assertEquals("test2.pdf", result.get(0));
    }

    private Stream<Path> buildStreamPaths() {
        return Stream.of(filePath1, filePath2, filePath3);
    }

    private FileStorageProps buildFileStorageProps() {
        final FileStorageProps fileStorageProps = new FileStorageProps();
        fileStorageProps.setPageSize(20L);
        fileStorageProps.setUploadMaxSize(1048576L);
        fileStorageProps.setBasePath("E:/test");
        return fileStorageProps;
    }

}