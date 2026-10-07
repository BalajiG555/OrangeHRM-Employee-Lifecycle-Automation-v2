package com.orangehrm.stepdefinitions;

import com.orangehrm.pages.PageManager;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Steps for role-based access control (menu visibility and direct-URL access).
 */
public class RoleBasedAccessSteps {

    private final PageManager pages;

    public RoleBasedAccessSteps(PageManager pages) {
        this.pages = pages;
    }

    @Then("the {string} menu should be {visibility}")
    public void menuVisibility(String menuItem, Boolean expectedVisible) {
        Assert.assertEquals(pages.sideMenu().isItemVisible(menuItem), expectedVisible.booleanValue(),
                "Visibility of the '" + menuItem + "' menu");
    }

    @When("I open the System Users page directly by URL")
    public void iOpenSystemUsersDirectly() {
        pages.systemUsersPage().openDirectly();
    }

    @Then("access to the System Users page should be {access}")
    public void systemUsersAccess(Boolean expectedGranted) {
        Assert.assertEquals(pages.systemUsersPage().isAccessible(), expectedGranted.booleanValue(),
                "Access to the System Users page");
    }
}
