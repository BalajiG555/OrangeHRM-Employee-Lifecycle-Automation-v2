package com.orangehrm.context;

import com.orangehrm.models.Employee;

/**
 * Scenario-scoped state shared between step-definition classes. Cucumber's PicoContainer creates one
 * instance per scenario and injects the same instance into every glue class, so state never leaks between
 * scenarios or parallel threads.
 */
public class TestContext {

    private Employee employee;
    private boolean failureEvidenceCaptured;

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    /**
     * Returns the scenario's employee or fails with a message pointing at the missing precondition step.
     */
    public Employee requireEmployee() {
        if (employee == null) {
            throw new IllegalStateException("No employee in the scenario context. Add 'Given an employee exists' "
                    + "or 'When I add a new employee with generated test data' before this step.");
        }
        return employee;
    }

    public boolean isFailureEvidenceCaptured() {
        return failureEvidenceCaptured;
    }

    public void markFailureEvidenceCaptured() {
        this.failureEvidenceCaptured = true;
    }
}
