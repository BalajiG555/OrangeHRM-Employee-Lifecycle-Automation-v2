package com.orangehrm.models;

/**
 * Login credentials. {@link #toString()} masks the password so credentials can be logged safely.
 *
 * @param username login name
 * @param password secret password
 */
public record Credentials(String username, String password) {

    @Override
    public String toString() {
        return "Credentials{username=" + username + ", password=****}";
    }
}
