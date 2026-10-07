package com.orangehrm.stepdefinitions;

import com.framework.api.ApiAssertions;
import com.orangehrm.api.ApiServices;
import com.orangehrm.context.TestContext;
import com.orangehrm.models.ApiEmployee;
import com.orangehrm.models.ApiJobDetails;
import com.orangehrm.models.Employee;
import com.orangehrm.services.EmployeeService;

import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

/**
 * Backend verification of changes made through the UI: status code, JSON-schema contract and field values.
 */
public class ApiVerificationSteps {

    private static final int HTTP_OK = 200;
    private static final String EMPLOYEE_SCHEMA = "schemas/employee.schema.json";
    private static final String JOB_DETAILS_SCHEMA = "schemas/employee-job-details.schema.json";

    private final ApiServices api;
    private final TestContext context;

    public ApiVerificationSteps(ApiServices api, TestContext context) {
        this.api = api;
        this.context = context;
    }

    @Then("the API should return the employee's personal details matching the UI data")
    public void apiReturnsPersonalDetails() {
        Employee expected = context.requireEmployee();
        ApiEmployee found = findOrFail(expected);

        Response response = api.employees().fetchEmployee(found.empNumber());
        ApiAssertions.assertStatus(response, HTTP_OK);
        ApiAssertions.assertMatchesSchema(response, EMPLOYEE_SCHEMA);
        ApiEmployee actual = EmployeeService.toApiEmployee(response);

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(actual.firstName(), expected.getFirstName(), "API firstName");
        softly.assertEquals(actual.lastName(), expected.getLastName(), "API lastName");
        softly.assertEquals(actual.employeeId(), expected.getEmployeeId(), "API employeeId");
        softly.assertEquals(actual.empNumber(), expected.getEmpNumber().intValue(), "API empNumber vs UI URL");
        softly.assertAll();
    }

    @Then("the API should return the updated Job Title and Employment Status")
    public void apiReturnsUpdatedJobDetails() {
        Employee expected = context.requireEmployee();
        ApiEmployee found = findOrFail(expected);

        Response response = api.employees().fetchJobDetails(found.empNumber());
        ApiAssertions.assertStatus(response, HTTP_OK);
        ApiAssertions.assertMatchesSchema(response, JOB_DETAILS_SCHEMA);
        ApiJobDetails actual = EmployeeService.toApiJobDetails(response);

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(actual.jobTitle(), expected.getJobTitle(), "API job title");
        softly.assertEquals(actual.employmentStatus(), expected.getEmploymentStatus(), "API employment status");
        softly.assertAll();
    }

    @Then("the API should no longer return the employee")
    public void apiNoLongerReturnsEmployee() {
        String employeeId = context.requireEmployee().getEmployeeId();
        Assert.assertFalse(api.employees().exists(employeeId),
                "Deleted employee " + employeeId + " should not be returned by the API.");
    }

    private ApiEmployee findOrFail(Employee expected) {
        return api.employees().findByEmployeeId(expected.getEmployeeId())
                .orElseThrow(() -> new AssertionError("Employee " + expected.getEmployeeId()
                        + " was not found through the API."));
    }
}
