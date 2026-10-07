package com.framework.utils;

import com.framework.exceptions.FrameworkException;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves classpath resources to absolute file paths, independent of the working directory.
 */
public final class ResourceUtils {

    private ResourceUtils() {
    }

    /**
     * Returns the absolute path of a classpath resource such as {@code testdata/profile.png}.
     */
    public static String absolutePath(String classpathResource) {
        URL url = ResourceUtils.class.getClassLoader().getResource(classpathResource);
        if (url == null) {
            throw new FrameworkException("Resource not found on classpath: " + classpathResource);
        }
        try {
            Path path = Paths.get(url.toURI());
            return path.toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            throw new FrameworkException("Invalid resource location for " + classpathResource, e);
        }
    }
}
