package com.framework.unit;

import com.framework.data.CleanupRegistry;
import com.framework.utils.XPathUtils;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Cleanup ordering / isolation and safe XPath literal building.
 */
public class FrameworkUtilitiesTest {

    @Test
    public void cleanupRunsInReverseOrderAndSurvivesFailures() {
        List<String> executed = new ArrayList<>();
        CleanupRegistry registry = new CleanupRegistry();
        registry.register("employee", () -> executed.add("employee"));
        registry.register("broken", () -> {
            throw new IllegalStateException("boom");
        });
        registry.register("user", () -> executed.add("user"));

        List<String> failures = registry.runAll();

        Assert.assertEquals(executed, List.of("user", "employee"), "LIFO order, failure does not stop the rest");
        Assert.assertEquals(failures.size(), 1);
        Assert.assertTrue(registry.isEmpty());
    }

    @Test
    public void xpathLiteralHandlesQuotes() {
        Assert.assertEquals(XPathUtils.literal("Smith"), "'Smith'");
        Assert.assertEquals(XPathUtils.literal("O'Brien"), "\"O'Brien\"");
        Assert.assertEquals(XPathUtils.literal("a'b\"c"), "concat('a', \"'\", 'b\"c')");
    }
}
