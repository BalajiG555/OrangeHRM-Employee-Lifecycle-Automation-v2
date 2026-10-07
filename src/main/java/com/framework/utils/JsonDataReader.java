package com.framework.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.config.ConfigReader;
import com.framework.exceptions.FrameworkException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads JSON test-data templates from the classpath.
 *
 * <p>Environment-specific data wins over the shared default: for {@code -Denv=qa}, the reader looks for
 * {@code testdata/qa/<file>} first and falls back to {@code testdata/<file>}. This lets an environment use
 * different reference data (job titles, statuses) without touching any test code.</p>
 */
public final class JsonDataReader {

    private static final String TESTDATA_DIR = "testdata/";
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonDataReader() {
    }

    /**
     * Deserialises {@code fileName} into a fresh instance of {@code type}.
     */
    public static <T> T read(String fileName, Class<T> type) {
        String environmentSpecific = TESTDATA_DIR + ConfigReader.getEnvironment() + "/" + fileName;
        String shared = TESTDATA_DIR + fileName;
        ClassLoader loader = JsonDataReader.class.getClassLoader();
        String resource = loader.getResource(environmentSpecific) != null ? environmentSpecific : shared;

        try (InputStream stream = loader.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new FrameworkException("Test data file not found on classpath: " + shared);
            }
            return MAPPER.readValue(stream, type);
        } catch (IOException e) {
            throw new FrameworkException("Unable to parse test data file " + resource, e);
        }
    }
}
