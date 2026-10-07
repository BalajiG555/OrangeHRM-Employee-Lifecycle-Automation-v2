package com.orangehrm.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Cucumber entry point. Scenarios are supplied through a parallel DataProvider; the thread count comes from
 * {@code parallel.threads} (set at runtime by {@code ParallelExecutionListener}, wired in testng.xml together
 * with the retry and flaky-detection listeners).
 *
 * <p>Default tag filter excludes work-in-progress and quarantined scenarios; override it with
 * {@code -Dcucumber.filter.tags="..."}.</p>
 */
@CucumberOptions(
        features = "classpath:features",
        glue = {"com.orangehrm.stepdefinitions", "com.orangehrm.hooks"},
        tags = "not @wip and not @quarantine",
        plugin = {
            "summary",
            "html:target/cucumber-reports/cucumber.html",
            "json:target/cucumber-reports/cucumber.json",
            "junit:target/cucumber-reports/cucumber-junit.xml",
            "rerun:target/cucumber-reports/rerun.txt",
            "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true)
public class TestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
