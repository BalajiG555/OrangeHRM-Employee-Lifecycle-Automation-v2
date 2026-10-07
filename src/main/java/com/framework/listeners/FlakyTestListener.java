package com.framework.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Flaky-test detection for in-run retries.
 *
 * <p>TestNG marks a failed attempt that is going to be retried as SKIPPED with {@code wasRetried()==true}.
 * This listener counts those attempts per scenario and classifies the final outcome:</p>
 * <ul>
 *     <li><b>FLAKY</b> - failed at least once, then passed (non-deterministic behaviour)</li>
 *     <li><b>CONSISTENT FAILURE</b> - failed on every attempt (likely a real defect)</li>
 * </ul>
 * <p>At the end of the run it writes {@code target/flaky-report/flaky-tests.md} and, on GitHub Actions,
 * appends it to the job summary so flakiness is visible on every pipeline run.</p>
 */
public class FlakyTestListener implements ITestListener, IExecutionListener {

    private static final Logger LOG = LoggerFactory.getLogger(FlakyTestListener.class);
    private static final Path REPORT = Paths.get("target", "flaky-report", "flaky-tests.md");

    private static final Map<String, AtomicInteger> FAILED_ATTEMPTS = new ConcurrentHashMap<>();
    private static final Map<String, Integer> FLAKY = new ConcurrentHashMap<>();
    private static final Map<String, Integer> CONSISTENT_FAILURES = new ConcurrentHashMap<>();

    @Override
    public void onTestSkipped(ITestResult result) {
        if (result.wasRetried()) {
            int attempts = FAILED_ATTEMPTS.computeIfAbsent(TestNames.of(result), key -> new AtomicInteger())
                    .incrementAndGet();
            LOG.warn("Attempt {} failed and will be retried: {}", attempts, TestNames.of(result));
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        int failedAttempts = failedAttempts(result);
        if (failedAttempts > 0) {
            FLAKY.put(TestNames.of(result), failedAttempts);
            LOG.warn("FLAKY: '{}' passed after {} failed attempt(s).", TestNames.of(result), failedAttempts);
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        int failedAttempts = failedAttempts(result);
        if (failedAttempts > 0) {
            CONSISTENT_FAILURES.put(TestNames.of(result), failedAttempts + 1);
            LOG.error("CONSISTENT FAILURE: '{}' failed on all {} attempt(s).", TestNames.of(result),
                    failedAttempts + 1);
        }
    }

    @Override
    public void onExecutionFinish() {
        String markdown = buildReport();
        try {
            Files.createDirectories(REPORT.getParent());
            Files.writeString(REPORT, markdown, StandardCharsets.UTF_8);
            String summary = System.getenv("GITHUB_STEP_SUMMARY");
            if (summary != null && !summary.isBlank()) {
                Files.writeString(Paths.get(summary), markdown, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            LOG.warn("Could not write flaky test report: {}", e.getMessage());
        }
    }

    private static int failedAttempts(ITestResult result) {
        AtomicInteger attempts = FAILED_ATTEMPTS.get(TestNames.of(result));
        return attempts == null ? 0 : attempts.get();
    }

    private static String buildReport() {
        List<String> lines = new ArrayList<>();
        lines.add("### Flaky test report");
        lines.add("");
        if (FLAKY.isEmpty() && CONSISTENT_FAILURES.isEmpty()) {
            lines.add("No retries were needed in this run.");
        } else {
            lines.add("| Scenario | Classification | Attempts |");
            lines.add("|---|---|---|");
            FLAKY.forEach((name, failed) ->
                    lines.add("| " + name + " | FLAKY (passed on retry) | " + (failed + 1) + " |"));
            CONSISTENT_FAILURES.forEach((name, attempts) ->
                    lines.add("| " + name + " | CONSISTENT FAILURE | " + attempts + " |"));
        }
        lines.add("");
        return String.join(System.lineSeparator(), lines);
    }
}
