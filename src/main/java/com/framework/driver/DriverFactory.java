package com.framework.driver;

import com.framework.config.ConfigKeys;
import com.framework.config.ConfigReader;
import com.framework.exceptions.ConfigurationException;
import com.framework.exceptions.FrameworkException;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;

/**
 * Thread-safe WebDriver lifecycle management. Each test thread owns exactly one driver, which is
 * what makes parallel scenario execution safe.
 *
 * <p>Runs locally by default; set {@code grid.url} (e.g. {@code http://localhost:4444}) to run on a
 * Selenium Grid such as the one in {@code docker-compose.yml}.</p>
 */
public final class DriverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static final String DEFAULT_WINDOW_SIZE = "1920x1080";
    private static final int DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS = 30;

    private DriverFactory() {
    }

    /**
     * Starts a browser for the current thread, replacing any driver that was left behind.
     */
    public static WebDriver initializeDriver() {
        if (DRIVER.get() != null) {
            LOG.warn("A driver was still bound to this thread; quitting it before creating a new one.");
            quitDriver();
        }
        BrowserType browser = BrowserType.from(ConfigReader.getRequired(ConfigKeys.BROWSER));
        boolean headless = ConfigReader.getBoolean(ConfigKeys.HEADLESS, false);
        BrowserType.WindowSize windowSize =
                BrowserType.WindowSize.parse(ConfigReader.get(ConfigKeys.WINDOW_SIZE, DEFAULT_WINDOW_SIZE));
        MutableCapabilities options = browser.createOptions(headless, windowSize);

        String gridUrl = ConfigReader.get(ConfigKeys.GRID_URL);
        WebDriver driver = gridUrl == null ? browser.createLocalDriver(options) : createRemoteDriver(gridUrl, options);

        driver.manage().timeouts()
                .implicitlyWait(Duration.ZERO)
                .pageLoadTimeout(Duration.ofSeconds(
                        ConfigReader.getInt(ConfigKeys.PAGE_LOAD_TIMEOUT_SECONDS, DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS)));

        DRIVER.set(driver);
        LOG.info("Started {} (headless={}, window={}x{}, grid={})", browser, headless,
                windowSize.width(), windowSize.height(), gridUrl == null ? "none" : gridUrl);
        return driver;
    }

    /**
     * Returns the driver bound to the current thread.
     *
     * @throws FrameworkException if no driver has been started for this thread
     */
    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new FrameworkException("WebDriver has not been initialised for thread "
                    + Thread.currentThread().getName() + ". Is the @Before hook running?");
        }
        return driver;
    }

    /**
     * Returns {@code true} when the current thread owns a live driver.
     */
    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    /**
     * Quits and unbinds the current thread's driver. Safe to call when no driver exists.
     */
    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            return;
        }
        try {
            driver.quit();
        } catch (WebDriverException e) {
            LOG.warn("Browser did not quit cleanly: {}", e.getMessage());
        } finally {
            DRIVER.remove();
        }
    }

    private static WebDriver createRemoteDriver(String gridUrl, MutableCapabilities options) {
        try {
            RemoteWebDriver remote = new RemoteWebDriver(URI.create(gridUrl).toURL(), options);
            // Uploads (e.g. the profile picture) must be streamed to the remote node.
            remote.setFileDetector(new LocalFileDetector());
            // Augmenting exposes DevTools (used for video capture) over Grid 4.
            return new Augmenter().augment(remote);
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new ConfigurationException("Invalid grid.url '" + gridUrl + "'.", e);
        }
    }
}
