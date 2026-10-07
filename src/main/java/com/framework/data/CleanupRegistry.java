package com.framework.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Per-scenario registry of teardown actions, executed in reverse (LIFO) order so dependent data is
 * removed before the data it depends on (e.g. a system user before its employee record).
 *
 * <p>Cleanup never throws: failures are logged and returned so the original scenario result is
 * preserved and a cleanup problem cannot mask a real test failure.</p>
 */
public final class CleanupRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(CleanupRegistry.class);

    private final Deque<CleanupTask> tasks = new ArrayDeque<>();

    /**
     * Registers an idempotent cleanup action.
     */
    public void register(String description, Runnable action) {
        tasks.push(new CleanupTask(description, action));
    }

    /**
     * Runs all registered actions (LIFO) and returns descriptions of those that failed.
     */
    public List<String> runAll() {
        List<String> failures = new ArrayList<>();
        while (!tasks.isEmpty()) {
            CleanupTask task = tasks.pop();
            try {
                task.action().run();
                LOG.info("Cleanup done: {}", task.description());
            } catch (RuntimeException e) {
                LOG.error("Cleanup failed: {}", task.description(), e);
                failures.add(task.description() + " -> " + e.getMessage());
            }
        }
        return failures;
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    private record CleanupTask(String description, Runnable action) {
    }
}
