package com.orangehrm.models;

/**
 * A system (login) user created for a test, linked to an employee record.
 *
 * @param userId      backend id of the system user
 * @param role        the user's role
 * @param credentials login credentials
 * @param employee    the employee the account belongs to
 */
public record SystemUser(int userId, UserRole role, Credentials credentials, Employee employee) {
}
