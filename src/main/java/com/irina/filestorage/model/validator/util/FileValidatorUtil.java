package com.irina.filestorage.model.validator.util;

import java.nio.file.Files;
import java.nio.file.Paths;

public class FileValidatorUtil {
    public static boolean fileExists(final String basePath, final String fileName) {
        return Files.exists(Paths.get(basePath, fileName).normalize());
    }
}
