package com.framework.listeners;

import io.cucumber.testng.Pickle;
import io.cucumber.testng.PickleWrapper;
import org.testng.ITestResult;

/**
 * Builds readable, stable identifiers for TestNG results. With Cucumber every scenario runs through the
 * same {@code runScenario} method, so the scenario (pickle) is taken from the data-provider parameters.
 */
public final class TestNames {

    private TestNames() {
    }

    /**
     * Returns e.g. {@code Admin deletes an employee [features/employee_management.feature:22]}.
     */
    public static String of(ITestResult result) {
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0 && parameters[0] instanceof PickleWrapper wrapper) {
            Pickle pickle = wrapper.getPickle();
            String uri = pickle.getUri().toString();
            String feature = uri.substring(uri.lastIndexOf('/') + 1);
            return pickle.getName() + " [" + feature + ":" + pickle.getLine() + "]";
        }
        return result.getMethod().getQualifiedName();
    }
}
