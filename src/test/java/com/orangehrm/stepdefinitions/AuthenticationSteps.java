package com.orangehrm.stepdefinitions;

import com.orangehrm.data.TestDataGenerator;
import com.orangehrm.models.UserRole;
import com.orangehrm.pages.PageManager;
import com.orangehrm.services.TestDataService;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Steps for login, logout and authentication failures.
 */
public class AuthenticationSteps {

    private final PageManager pages;
    private final TestDataService testData;

    public AuthenticationSteps(PageManager pages, TestDataService testData) {
        this.pages = pages;
        this.testData = testData;
    }

    @Given("I am on the OrangeHRM login page")
    public void iAmOnTheLoginPage() {
        Assert.assertTrue(pages.loginPage().isLoaded(), "The OrangeHRM login page should be displayed.");
    }

    @When("I log in as {role}")
    public void iLogInAs(UserRole role) {
        pages.loginPage().loginAs(testData.credentialsFor(role));
    }

    /**
     * Precondition variant: logs in and verifies it worked, so later failures are never caused by a silent
     * login problem.
     */
    @Given("I am logged in as {role}")
    public void iAmLoggedInAs(UserRole role) {
        iLogInAs(role);
        iShouldLandOnTheDashboard();
    }

    @When("I log in with invalid credentials")
    public void iLogInWithInvalidCredentials() {
        pages.loginPage().attemptLogin(TestDataGenerator.invalidCredentials());
    }

    @When("I log out")
    public void iLogOut() {
        pages.dashboardPage().logout();
    }

    @Then("I should land on the Dashboard")
    public void iShouldLandOnTheDashboard() {
        Assert.assertTrue(pages.dashboardPage().isLoaded(), "The Dashboard should be displayed after login.");
    }

    @Then("I should see the login error message {string}")
    public void iShouldSeeTheLoginError(String expectedMessage) {
        Assert.assertEquals(pages.loginPage().getErrorMessage(), expectedMessage, "Login error message");
    }

    @Then("I should be on the login page")
    public void iShouldBeOnTheLoginPage() {
        Assert.assertTrue(pages.loginPage().isLoaded(), "The login page should be displayed.");
    }
}
