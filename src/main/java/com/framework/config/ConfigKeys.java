package com.framework.config;

/**
 * Central catalogue of configuration keys. Keeping keys here avoids scattering string
 * literals across the code base and makes every tunable discoverable in one place.
 *
 * <p>Every key can be overridden by a JVM system property (e.g. {@code -Dheadless=true}) or an
 * {@code ORANGEHRM_*} environment variable (e.g. {@code ORANGEHRM_HEADLESS=true}).</p>
 */
public final class ConfigKeys {

    /* Environment selection. */
    public static final String ENV = "env";

    /* Application under test. */
    public static final String APP_BASE_URL = "app.base.url";
    public static final String API_BASE_URL = "api.base.url";

    /* Browser / driver. */
    public static final String BROWSER = "browser";
    public static final String HEADLESS = "headless";
    public static final String WINDOW_SIZE = "browser.window.size";
    public static final String GRID_URL = "grid.url";

    /* Synchronisation. */
    public static final String EXPLICIT_WAIT_SECONDS = "explicit.wait.seconds";
    public static final String SHORT_WAIT_SECONDS = "short.wait.seconds";
    public static final String PAGE_LOAD_TIMEOUT_SECONDS = "page.load.timeout.seconds";

    /* Credentials (supplied via local config.properties or CI secrets, never committed). */
    public static final String ADMIN_USERNAME = "username";
    public static final String ADMIN_PASSWORD = "password";

    /* API. */
    public static final String API_VALIDATION_ENABLED = "api.validation.enabled";
    public static final String API_AUTH_MODE = "api.auth.mode";
    public static final String API_ACCESS_TOKEN = "api.access.token";
    public static final String API_CLIENT_ID = "api.client.id";
    public static final String API_CLIENT_SECRET = "api.client.secret";
    public static final String API_REDIRECT_URI = "api.redirect.uri";
    public static final String API_AUTHORIZATION_CODE = "api.authorization.code";
    public static final String API_CODE_VERIFIER = "api.code.verifier";

    /* Execution behaviour. */
    public static final String PARALLEL_THREADS = "parallel.threads";
    public static final String RETRY_MAX_COUNT = "retry.max.count";
    public static final String DESTRUCTIVE_TESTS_ENABLED = "tests.destructive.enabled";

    /* Evidence / observability. */
    public static final String VIDEO_MODE = "video.mode";
    public static final String VIDEO_MAX_FRAMES = "video.max.frames";
    public static final String ARTIFACTS_DIR = "artifacts.dir";

    /* Test data. */
    public static final String TESTDATA_SETUP_MODE = "testdata.setup.mode";
    public static final String EMPLOYEE_ID_PREFIX = "testdata.employee.id.prefix";

    private ConfigKeys() {
    }
}
