package com.framework.unit.fixtures;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixture used by {@code FlakyDetectionTest}: fails on its first invocation and passes on the second,
 * simulating a flaky test. Not part of any suite; it is run programmatically.
 */
public class FlakyOnceSample {

    private static final AtomicInteger INVOCATIONS = new AtomicInteger();

    @Test
    public void passesOnSecondAttempt() {
        Assert.assertTrue(INVOCATIONS.incrementAndGet() > 1, "Simulated intermittent failure");
    }

    @Test
    public void alwaysFails() {
        Assert.fail("Simulated real defect");
    }
}
