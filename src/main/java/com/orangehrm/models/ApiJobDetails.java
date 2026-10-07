package com.orangehrm.models;

/**
 * Employee job details as returned by the backend API.
 *
 * @param jobTitle         job title name (null when not set)
 * @param employmentStatus employment status name (null when not set)
 */
public record ApiJobDetails(String jobTitle, String employmentStatus) {
}
