package com.framework.reporting;

import com.framework.driver.DriverFactory;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Captures screenshots of the current thread's browser. Capturing never throws: missing evidence must
 * not turn a meaningful failure into a confusing secondary one.
 */
public final class ScreenshotUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ScreenshotUtils.class);

    private ScreenshotUtils() {
    }

    /**
     * Returns PNG bytes, or empty when there is no live browser or capture fails.
     */
    public static Optional<byte[]> capture() {
        if (!DriverFactory.hasDriver()) {
            return Optional.empty();
        }
        try {
            return Optional.of(((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.BYTES));
        } catch (WebDriverException e) {
            LOG.warn("Screenshot capture failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Saves PNG bytes under {@code artifacts.dir/screenshots} so CI can publish them as artifacts.
     */
    public static Optional<Path> saveToDisk(byte[] png, String scenarioName) {
        Path target = ArtifactPaths.screenshots().resolve(ArtifactPaths.fileStem(scenarioName) + ".png");
        try {
            return Optional.of(Files.write(target, png));
        } catch (IOException e) {
            LOG.warn("Could not write screenshot to {}: {}", target, e.getMessage());
            return Optional.empty();
        }
    }
}
