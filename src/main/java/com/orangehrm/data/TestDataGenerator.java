package com.orangehrm.data;

import com.framework.config.ConfigKeys;
import com.framework.config.ConfigReader;
import com.framework.exceptions.ConfigurationException;
import com.framework.utils.JsonDataReader;
import com.framework.utils.RandomDataUtils;
import com.orangehrm.models.Credentials;
import com.orangehrm.models.Employee;
import com.orangehrm.models.UserRole;

import java.util.Locale;

/**
 * Generates unique, collision-free test data from JSON templates.
 *
 * <p>Uniqueness comes from {@link java.security.SecureRandom} rather than timestamps: scenarios running in
 * parallel within the same second would otherwise receive identical ids. A per-environment prefix
 * ({@code testdata.employee.id.prefix}) makes test records easy to identify and purge.</p>
 */
public final class TestDataGenerator {

    private static final String EMPLOYEE_TEMPLATE = "employee.json";
    /** OrangeHRM limits Employee Id to 10 characters. */
    private static final int EMPLOYEE_ID_MAX_LENGTH = 10;
    private static final int NAME_SUFFIX_LENGTH = 6;
    private static final int USERNAME_SUFFIX_LENGTH = 10;
    private static final int PASSWORD_LENGTH = 14;
    private static final int INVALID_VALUE_LENGTH = 12;

    private TestDataGenerator() {
    }

    /**
     * Returns a new employee built from {@code testdata/employee.json} with unique name and id.
     */
    public static Employee employee() {
        Employee employee = JsonDataReader.read(EMPLOYEE_TEMPLATE, Employee.class);
        String suffix = capitalize(RandomDataUtils.letters(NAME_SUFFIX_LENGTH));
        employee.setFirstName(employee.getFirstName() + suffix);
        employee.setLastName(employee.getLastName() + suffix);
        employee.setEmployeeId(employeeId());
        return employee;
    }

    /**
     * Returns unique credentials for a dedicated test account.
     */
    public static Credentials userCredentials(UserRole role) {
        String username = role.label().toLowerCase(Locale.ROOT) + "_"
                + RandomDataUtils.lowercaseAlphanumeric(USERNAME_SUFFIX_LENGTH);
        return new Credentials(username, RandomDataUtils.strongPassword(PASSWORD_LENGTH));
    }

    /**
     * Returns credentials that are guaranteed not to exist.
     */
    public static Credentials invalidCredentials() {
        return new Credentials("invalid_" + RandomDataUtils.lowercaseAlphanumeric(INVALID_VALUE_LENGTH),
                RandomDataUtils.strongPassword(INVALID_VALUE_LENGTH));
    }

    private static String employeeId() {
        String prefix = ConfigReader.get(ConfigKeys.EMPLOYEE_ID_PREFIX, "AT");
        int digits = EMPLOYEE_ID_MAX_LENGTH - prefix.length();
        if (digits < NAME_SUFFIX_LENGTH) {
            throw new ConfigurationException("testdata.employee.id.prefix '" + prefix + "' is too long; "
                    + "keep it to " + (EMPLOYEE_ID_MAX_LENGTH - NAME_SUFFIX_LENGTH) + " characters or fewer.");
        }
        return prefix + RandomDataUtils.numeric(digits);
    }

    private static String capitalize(String value) {
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
