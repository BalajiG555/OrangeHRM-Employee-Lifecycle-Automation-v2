package com.framework.listeners;

import com.framework.reporting.AllureEnvironmentWriter;

import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * Writes Allure environment / executor / category metadata once at suite start.
 */
public class ReportEnvironmentListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        AllureEnvironmentWriter.write();
    }
}
