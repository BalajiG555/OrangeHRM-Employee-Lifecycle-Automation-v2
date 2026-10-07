package com.framework.config;

import com.framework.exceptions.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Layered, environment-aware configuration.
 *
 * <p>Resolution order (highest priority first):</p>
 * <ol>
 *     <li>JVM system property, e.g. {@code -Dbrowser=firefox}</li>
 *     <li>{@code ORANGEHRM_*} environment variable, e.g. {@code ORANGEHRM_BROWSER=firefox}</li>
 *     <li>{@code config/config.properties} - optional, git-ignored, local secrets/overrides</li>
 *     <li>{@code config/config-<env>.properties} - committed, environment specific</li>
 *     <li>{@code config/config-default.properties} - committed, shared defaults</li>
 * </ol>
 *
 * <p>The environment is chosen with {@code -Denv=qa} (or {@code ORANGEHRM_ENV=qa}) and defaults to
 * {@code local}. An unknown environment fails fast instead of silently running against defaults.</p>
 */
public final class ConfigReader {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigReader.class);

    private static final String ENV_VAR_PREFIX = "ORANGEHRM_";
    private static final String DEFAULT_ENVIRONMENT = "local";
    private static final String CONFIG_DIR = "config/";
    private static final String DEFAULT_FILE = CONFIG_DIR + "config-default.properties";
    private static final String LOCAL_OVERRIDE_FILE = CONFIG_DIR + "config.properties";

    private static final String ENVIRONMENT = resolveEnvironment();
    private static final Properties PROPERTIES = loadProperties();

    private ConfigReader() {
    }

    /**
     * Returns the active environment name (lower case).
     */
    public static String getEnvironment() {
        return ENVIRONMENT;
    }

    /**
     * Returns the value for {@code key}, or {@code null} when it is not configured anywhere.
     */
    public static String get(String key) {
        String systemProperty = System.getProperty(key);
        if (isNotBlank(systemProperty)) {
            return systemProperty.trim();
        }
        String environmentVariable = System.getenv(toEnvironmentVariable(key));
        if (isNotBlank(environmentVariable)) {
            return environmentVariable.trim();
        }
        String fileValue = PROPERTIES.getProperty(key);
        return isNotBlank(fileValue) ? fileValue.trim() : null;
    }

    /**
     * Returns the value for {@code key}, or {@code defaultValue} when it is not configured.
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : value;
    }

    /**
     * Returns the value for {@code key} or fails fast with an actionable message.
     */
    public static String getRequired(String key) {
        String value = get(key);
        if (value == null) {
            throw new ConfigurationException(String.format(
                    "Required configuration '%s' is missing for environment '%s'. Provide it via -D%s=..., "
                            + "the %s environment variable, or src/test/resources/%s.",
                    key, ENVIRONMENT, key, toEnvironmentVariable(key), LOCAL_OVERRIDE_FILE));
        }
        return value;
    }

    /**
     * Returns a boolean value, or {@code defaultValue} when it is not configured.
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    /**
     * Returns an integer value, or {@code defaultValue} when it is not configured.
     */
    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ConfigurationException(
                    "Configuration '" + key + "' must be an integer but was '" + value + "'.", e);
        }
    }

    /**
     * Converts a property key into its environment-variable form: {@code api.base.url -> ORANGEHRM_API_BASE_URL}.
     */
    static String toEnvironmentVariable(String key) {
        return ENV_VAR_PREFIX + key.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT);
    }

    private static String resolveEnvironment() {
        String value = System.getProperty(ConfigKeys.ENV);
        if (!isNotBlank(value)) {
            value = System.getenv(ENV_VAR_PREFIX + "ENV");
        }
        return isNotBlank(value) ? value.trim().toLowerCase(Locale.ROOT) : DEFAULT_ENVIRONMENT;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        loadFile(properties, DEFAULT_FILE, true);
        loadFile(properties, CONFIG_DIR + "config-" + ENVIRONMENT + ".properties", true);
        loadFile(properties, LOCAL_OVERRIDE_FILE, false);
        LOG.info("Configuration loaded for environment '{}'", ENVIRONMENT);
        return properties;
    }

    private static void loadFile(Properties target, String resource, boolean required) {
        try (InputStream stream = ConfigReader.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                if (required) {
                    throw new ConfigurationException("Configuration file not found on classpath: " + resource
                            + ". Check the -Denv value (supported: local, dev, qa, prod).");
                }
                LOG.debug("Optional configuration file not present: {}", resource);
                return;
            }
            target.load(stream);
            LOG.debug("Loaded configuration file {}", resource);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read configuration file: " + resource, e);
        }
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
