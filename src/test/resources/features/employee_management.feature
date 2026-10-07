@employee @ui @destructive
Feature: Employee management (PIM)
  As an HR administrator
  I want to add, update and remove employees
  So that the employee register stays accurate

  Background:
    Given I am logged in as Admin

  @smoke @regression @severity=critical
  Scenario: Admin adds a new employee with a profile picture
    When I add a new employee with generated test data
    Then the employee's Personal Details page should show the new employee
    And the employee should be listed when searching by Employee ID

  @regression @severity=normal
  Scenario: Admin updates an employee's Job Title and Employment Status
    Given an employee exists
    When I update the employee's Job Title and Employment Status
    Then the Job details should show the updated values

  @regression @severity=critical
  Scenario: Admin deletes an employee
    Given an employee exists
    When I delete the employee from the Employee List
    Then the employee should no longer be listed in the Employee List
