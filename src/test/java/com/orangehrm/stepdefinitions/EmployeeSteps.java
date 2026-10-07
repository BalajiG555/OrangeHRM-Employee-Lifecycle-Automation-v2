package com.orangehrm.stepdefinitions;

import com.orangehrm.context.TestContext;
import com.orangehrm.models.Employee;
import com.orangehrm.pages.EmployeeDetailsPage;
import com.orangehrm.pages.EmployeeJobPage;
import com.orangehrm.pages.EmployeeListPage;
import com.orangehrm.pages.PageManager;
import com.orangehrm.services.TestDataService;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

/**
 * UI steps for the employee lifecycle (create, search, update, delete).
 */
public class EmployeeSteps {

    private final PageManager pages;
    private final TestDataService testData;
    private final TestContext context;

    public EmployeeSteps(PageManager pages, TestDataService testData, TestContext context) {
        this.pages = pages;
        this.testData = testData;
        this.context = context;
    }

    /**
     * Precondition: a unique employee exists. Seeded through the API by default so scenarios that test
     * update/delete do not depend on (or re-test) the Add Employee UI.
     */
    @Given("an employee exists")
    public void anEmployeeExists() {
        Employee employee = testData.isApiSetupEnabled()
                ? testData.seedEmployee()
                : createThroughUi(testData.newEmployee());
        context.setEmployee(employee);
    }

    @When("I add a new employee with generated test data")
    public void iAddANewEmployee() {
        context.setEmployee(createThroughUi(testData.newEmployee()));
    }

    @When("I update the employee's Job Title and Employment Status")
    public void iUpdateJobDetails() {
        Employee employee = context.requireEmployee();
        openJobPage(employee).updateJobDetails(employee.getJobTitle(), employee.getEmploymentStatus());
    }

    @When("I delete the employee from the Employee List")
    public void iDeleteTheEmployee() {
        String employeeId = context.requireEmployee().getEmployeeId();
        pages.employeeListPage().open().searchByEmployeeId(employeeId).deleteEmployee(employeeId);
    }

    @Then("the employee's Personal Details page should show the new employee")
    public void personalDetailsShowNewEmployee() {
        Employee employee = context.requireEmployee();
        EmployeeDetailsPage details = pages.employeeDetailsPage();
        SoftAssert softly = new SoftAssert();
        softly.assertTrue(details.isLoaded(), "Personal Details page should be displayed.");
        softly.assertEquals(details.getFirstName(), employee.getFirstName(), "First name");
        softly.assertEquals(details.getLastName(), employee.getLastName(), "Last name");
        softly.assertEquals(details.getEmpNumber(), employee.getEmpNumber().intValue(), "empNumber in URL");
        softly.assertAll();
    }

    @Then("the employee should be listed when searching by Employee ID")
    public void employeeIsListed() {
        String employeeId = context.requireEmployee().getEmployeeId();
        EmployeeListPage list = pages.employeeListPage().open().searchByEmployeeId(employeeId);
        Assert.assertTrue(list.isListed(employeeId), "Employee " + employeeId + " should appear in the list.");
    }

    @Then("the Job details should show the updated values")
    public void jobDetailsShowUpdatedValues() {
        Employee employee = context.requireEmployee();
        EmployeeJobPage jobPage = openJobPage(employee);
        SoftAssert softly = new SoftAssert();
        softly.assertEquals(jobPage.getJobTitle(), employee.getJobTitle(), "Job Title");
        softly.assertEquals(jobPage.getEmploymentStatus(), employee.getEmploymentStatus(), "Employment Status");
        softly.assertAll();
    }

    @Then("the employee should no longer be listed in the Employee List")
    public void employeeIsNoLongerListed() {
        String employeeId = context.requireEmployee().getEmployeeId();
        EmployeeListPage list = pages.employeeListPage().open().searchByEmployeeId(employeeId);
        SoftAssert softly = new SoftAssert();
        softly.assertFalse(list.isListed(employeeId), "Deleted employee " + employeeId + " should not be listed.");
        softly.assertTrue(list.isNoRecordsMessageShown(), "'No Records Found' should be shown.");
        softly.assertAll();
    }

    private Employee createThroughUi(Employee employee) {
        testData.registerEmployeeForCleanup(employee);
        EmployeeDetailsPage details = pages.addEmployeePage().open().createEmployee(employee);
        employee.setEmpNumber(details.getEmpNumber());
        return employee;
    }

    private EmployeeJobPage openJobPage(Employee employee) {
        return pages.employeeListPage().open()
                .searchByEmployeeId(employee.getEmployeeId())
                .openEmployee(employee.getEmployeeId())
                .openJobTab();
    }
}
