package com.orangehrm.models;

/**
 * Employee personal data as returned by the backend API.
 *
 * @param empNumber  internal primary key
 * @param employeeId business employee id
 * @param firstName  first name
 * @param lastName   last name
 */
public record ApiEmployee(int empNumber, String employeeId, String firstName, String lastName) {
}
