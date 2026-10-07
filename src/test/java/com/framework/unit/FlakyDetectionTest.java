package com.framework.unit;

import com.framework.config.ConfigKeys;
import com.framework.listeners.FlakyTestListener;
import com.framework.listeners.RetryListener;
import com.framework.unit.fixtures.FlakyOnceSample;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Proves the retry + flaky-detection wiring end to end on a real TestNG run: a test that fails once then
 * passes must be classified FLAKY, a test that always fails must be classified CONSISTENT FAILURE.
 */
public class FlakyDetectionTest {

    private String previousRetry;

    @BeforeClass
    public void enableOneRetry() {
        previousRetry = System.getProperty(ConfigKeys.RETRY_MAX_COUNT);
        System.setProperty(ConfigKeys.RETRY_MAX_COUNT, "1");
    }

    @AfterClass(alwaysRun = true)
    public void restoreRetry() {
        if (previousRetry == null) {
            System.clearProperty(ConfigKeys.RETRY_MAX_COUNT);
        } else {
            System.setProperty(ConfigKeys.RETRY_MAX_COUNT, previousRetry);
        }
    }

    @Test
    public void classifiesFlakyAndConsistentFailures() throws IOException {
        TestNG testng = new TestNG();
        testng.setUseDefaultListeners(false);
        testng.setVerbose(0);
        testng.setOutputDirectory("target/unit-fixture-output");
        testng.setTestClasses(new Class<?>[] {FlakyOnceSample.class});
        testng.addListener(new RetryListener());
        FlakyTestListener listener = new FlakyTestListener();
        testng.addListener(listener);
        testng.run();
        listener.onExecutionFinish();

        String report = Files.readString(Paths.get("target", "flaky-report", "flaky-tests.md"), StandardCharsets.UTF_8);
        Assert.assertTrue(report.contains("passesOnSecondAttempt | FLAKY (passed on retry) | 2"),
                "Flaky test should be detected. Report:\n" + report);
        Assert.assertTrue(report.contains("alwaysFails | CONSISTENT FAILURE | 2"),
                "Consistent failure should be detected. Report:\n" + report);
    }
}
