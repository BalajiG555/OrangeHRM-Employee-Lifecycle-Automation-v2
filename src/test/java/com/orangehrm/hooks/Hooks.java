package com.orangehrm.hooks;

import com.framework.config.ConfigKeys;
import com.framework.config.ConfigReader;
import com.framework.driver.DriverFactory;
import com.framework.reporting.ArtifactPaths;
import com.framework.reporting.ScreenshotUtils;
import com.framework.reporting.video.VideoEncoder;
import com.framework.reporting.video.VideoFrame;
import com.framework.reporting.video.VideoMode;
import com.framework.reporting.video.VideoRecorder;
import com.orangehrm.context.TestContext;
import com.orangehrm.pages.PageManager;
import com.orangehrm.services.TestDataService;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.testng.SkipException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Scenario fixtures (setup / teardown).
 *
 * <ul>
 *     <li>Order 0: execution guards - skip destructive scenarios where data must not change (prod) and API
 *     scenarios when API validation is disabled. A skipped scenario is reported as skipped, never as a
 *     false pass.</li>
 *     <li>Order 10: browser start, login page, optional video recording.</li>
 *     <li>After each failed step: screenshot (embedded in both the Cucumber and Allure reports).</li>
 *     <li>After: failure diagnostics, video, API data cleanup, browser quit - each isolated so one failing
 *     teardown action never prevents the others or masks the scenario result.</li>
 * </ul>
 */
public class Hooks {

    private static final Logger LOG = LoggerFactory.getLogger(Hooks.class);
    private static final String MDC_SCENARIO = "scenario";
    private static final int DEFAULT_MAX_VIDEO_FRAMES = 900;

    private final TestContext context;
    private final TestDataService testData;
    private final PageManager pages;
    private VideoRecorder videoRecorder;

    public Hooks(TestContext context, TestDataService testData, PageManager pages) {
        this.context = context;
        this.testData = testData;
        this.pages = pages;
    }

    @Before(order = 0)
    public void logScenarioStart(Scenario scenario) {
        MDC.put(MDC_SCENARIO, scenario.getName());
        LOG.info("Starting scenario '{}' {} [env={}]", scenario.getName(), scenario.getSourceTagNames(),
                ConfigReader.getEnvironment());
    }

    @Before(value = "@destructive", order = 1)
    public void guardDestructiveScenario() {
        if (!ConfigReader.getBoolean(ConfigKeys.DESTRUCTIVE_TESTS_ENABLED, true)) {
            throw new SkipException("Skipped: scenario creates/changes data and tests.destructive.enabled=false "
                    + "for environment '" + ConfigReader.getEnvironment() + "'.");
        }
    }

    @Before(value = "@api", order = 1)
    public void guardApiScenario() {
        if (!ConfigReader.getBoolean(ConfigKeys.API_VALIDATION_ENABLED, true)) {
            throw new SkipException("Skipped: api.validation.enabled=false for environment '"
                    + ConfigReader.getEnvironment() + "'.");
        }
    }

    @Before(order = 10)
    public void startBrowser() {
        WebDriver driver = DriverFactory.initializeDriver();
        pages.loginPage().open();
        if (videoMode().isEnabled()) {
            videoRecorder = VideoRecorder.start(driver,
                    ConfigReader.getInt(ConfigKeys.VIDEO_MAX_FRAMES, DEFAULT_MAX_VIDEO_FRAMES)).orElse(null);
        }
    }

    @AfterStep
    public void captureFailedStep(Scenario scenario) {
        if (scenario.isFailed() && !context.isFailureEvidenceCaptured()) {
            attachScreenshot(scenario, "Screenshot at failed step");
            context.markFailureEvidenceCaptured();
        }
    }

    @After
    public void tearDown(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                runQuietly("failure diagnostics", () -> attachFailureDiagnostics(scenario));
            }
            runQuietly("video", () -> finishVideo(scenario));
            runQuietly("test data cleanup", this::cleanupTestData);
            runQuietly("browser quit", DriverFactory::quitDriver);
        } finally {
            LOG.info("Finished scenario '{}' with status {}", scenario.getName(), scenario.getStatus());
            MDC.remove(MDC_SCENARIO);
        }
    }

    private void attachFailureDiagnostics(Scenario scenario) {
        if (!context.isFailureEvidenceCaptured()) {
            attachScreenshot(scenario, "Screenshot at failure");
            context.markFailureEvidenceCaptured();
        }
        if (DriverFactory.hasDriver()) {
            WebDriver driver = DriverFactory.getDriver();
            scenario.attach(driver.getCurrentUrl(), "text/uri-list", "URL at failure");
            String source = driver.getPageSource();
            if (source != null) {
                scenario.attach(source.getBytes(StandardCharsets.UTF_8), "text/plain", "Page source at failure");
            }
        }
    }

    private void attachScreenshot(Scenario scenario, String name) {
        Optional<byte[]> screenshot = ScreenshotUtils.capture();
        screenshot.ifPresent(png -> {
            scenario.attach(png, "image/png", name);
            ScreenshotUtils.saveToDisk(png, scenario.getName());
        });
    }

    private void finishVideo(Scenario scenario) {
        if (videoRecorder == null) {
            return;
        }
        List<VideoFrame> frames = videoRecorder.stop();
        videoRecorder = null;
        if (!videoMode().shouldKeep(scenario.isFailed()) || frames.isEmpty()) {
            return;
        }
        VideoEncoder encoder = VideoEncoder.best();
        Path stem = ArtifactPaths.videos().resolve(ArtifactPaths.fileStem(scenario.getName()));
        encoder.encode(frames, stem).ifPresent(video -> attachVideo(video, encoder));
    }

    private static void attachVideo(Path video, VideoEncoder encoder) {
        try (InputStream stream = Files.newInputStream(video)) {
            Allure.addAttachment("Scenario recording", encoder.mimeType(), stream, encoder.extension());
            LOG.info("Video saved: {}", video);
        } catch (IOException e) {
            LOG.warn("Could not attach video {}: {}", video, e.getMessage());
        }
    }

    private void cleanupTestData() {
        List<String> failures = testData.cleanup();
        if (!failures.isEmpty()) {
            Allure.addAttachment("Cleanup failures", String.join(System.lineSeparator(), failures));
        }
    }

    private static VideoMode videoMode() {
        return VideoMode.from(ConfigReader.get(ConfigKeys.VIDEO_MODE, "off"));
    }

    private static void runQuietly(String action, Runnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException e) {
            if (e instanceof WebDriverException) {
                LOG.warn("Teardown step '{}' failed (browser issue): {}", action, e.getMessage());
            } else {
                LOG.error("Teardown step '{}' failed; the scenario result is preserved.", action, e);
            }
        }
    }
}
