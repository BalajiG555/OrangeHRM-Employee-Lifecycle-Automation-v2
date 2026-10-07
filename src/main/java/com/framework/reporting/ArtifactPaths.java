package com.framework.reporting;

import com.framework.config.ConfigKeys;
import com.framework.config.ConfigReader;
import com.framework.exceptions.FrameworkException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Locations of on-disk test evidence (screenshots, videos). CI uploads {@code artifacts.dir} as a build artifact.
 */
public final class ArtifactPaths {

    private static final String DEFAULT_ARTIFACTS_DIR = "target/test-artifacts";
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final int MAX_NAME_LENGTH = 80;

    private ArtifactPaths() {
    }

    public static Path screenshots() {
        return directory("screenshots");
    }

    public static Path videos() {
        return directory("videos");
    }

    /**
     * Converts a scenario name into a safe, unique-ish file name stem.
     */
    public static String fileStem(String scenarioName) {
        String safe = scenarioName.replaceAll("[^A-Za-z0-9-_]+", "_");
        if (safe.length() > MAX_NAME_LENGTH) {
            safe = safe.substring(0, MAX_NAME_LENGTH);
        }
        return safe + "_" + LocalDateTime.now().format(STAMP) + "_" + Thread.currentThread().threadId();
    }

    private static Path directory(String name) {
        Path path = Paths.get(ConfigReader.get(ConfigKeys.ARTIFACTS_DIR, DEFAULT_ARTIFACTS_DIR), name);
        try {
            return Files.createDirectories(path);
        } catch (IOException e) {
            throw new FrameworkException("Cannot create artifacts directory " + path, e);
        }
    }
}
