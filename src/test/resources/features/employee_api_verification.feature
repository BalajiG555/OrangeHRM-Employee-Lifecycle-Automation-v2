@api @employee @ui @destructive
Feature: Employee data integrity between UI and backend API
  Changes made through the UI must be persisted correctly in the backend.
  Each response is checked for status, JSON-schema contract and field values.

  Background:
    Given I am logged in as Admin

  @regression @severity=critical
  Scenario: Employee created through the UI is persisted in the backend
    When I add a new employee with generated test data
    Then the API should return the employee's personal details matching the UI data

  @regression @severity=critical
  Scenario: Job details updated through the UI are persisted in the backend
    Given an employee exists
    When I update the employee's Job Title and Employment Status
    Then the API should return the updated Job Title and Employment Status

  @regression @severity=critical
  Scenario: Employee deleted through the UI is removed from the backend
    Given an employee exists
    When I delete the employee from the Employee List
    Then the API should no longer return the employee
