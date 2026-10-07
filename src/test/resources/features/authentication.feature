@auth @ui
Feature: Authentication
  As an OrangeHRM user
  I want to sign in and out securely
  So that only authorised people can access HR data

  @smoke @regression @severity=blocker
  Scenario: Admin signs in with valid credentials
    Given I am on the OrangeHRM login page
    When I log in as Admin
    Then I should land on the Dashboard

  @regression @severity=critical
  Scenario: Sign-in is rejected for invalid credentials
    Given I am on the OrangeHRM login page
    When I log in with invalid credentials
    Then I should see the login error message "Invalid credentials"
    And I should be on the login page

  @smoke @regression @severity=critical
  Scenario: Signed-in user can log out
    Given I am logged in as Admin
    When I log out
    Then I should be on the login page
