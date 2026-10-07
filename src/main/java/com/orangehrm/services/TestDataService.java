package com.orangehrm.services;

import com.framework.config.ConfigKeys;
import com.framework.config.ConfigReader;
import com.framework.data.CleanupRegistry;
import com.orangehrm.api.ApiServices;
import com.orangehrm.data.CredentialsProvider;
import com.orangehrm.data.TestDataGenerator;
import com.orangehrm.models.Credentials;
import com.orangehrm.models.Employee;
import com.orangehrm.models.SystemUser;
import com.orangehrm.models.UserRole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

/**
 * Scenario-scoped test-data lifecycle: generate unique data, seed it through the API (fast, no UI
 * dependency), and guarantee cleanup through the API at the end of the scenario.
 *
 * <p>Every cleanup action is idempotent (it looks the record up before deleting), so it is safe even
 * when the scenario itself already deleted the data or failed half-way through creating it.</p>
 */
public class TestDataService {

    private static final Logger LOG = LoggerFactory.getLogger(TestDataService.class);

    private final ApiServices api;
    private final CleanupRegistry cleanup = new CleanupRegistry();

    public TestDataService(ApiServices api) {
        this.api = api;
    }

    /**
     * Returns fresh, unique employee data (not yet persisted).
     */
    public Employee newEmployee() {
        return TestDataGenerator.employee();
    }

    /**
     * {@code true} when preconditions should be seeded via API ({@code testdata.setup.mode=api}).
     */
    public boolean isApiSetupEnabled() {
        return "api".equals(ConfigReader.get(ConfigKeys.TESTDATA_SETUP_MODE, "api").toLowerCase(Locale.ROOT));
    }

    /**
     * Creates a unique employee via API and registers it for cleanup.
     */
    public Employee seedEmployee() {
        Employee employee = newEmployee();
        registerEmployeeForCleanup(employee);
        api.employees().create(employee);
        LOG.info("Seeded employee via API: {}", employee);
        return employee;
    }

    /**
     * Registers an employee (created through any channel) for idempotent API cleanup.
     * Register <em>before</em> creating so partially-created data is still removed.
     */
    public void registerEmployeeForCleanup(Employee employee) {
        String employeeId = employee.getEmployeeId();
        cleanup.register("Delete employee " + employeeId, () ->
                api.employees().findByEmployeeId(employeeId)
                        .ifPresent(found -> api.employees().delete(found.empNumber())));
    }

    /**
     * Returns login credentials for the role, creating a dedicated account for non-admin roles.
     */
    public Credentials credentialsFor(UserRole role) {
        return role == UserRole.ADMIN ? CredentialsProvider.admin() : seedSystemUser(role).credentials();
    }

    /**
     * Creates an employee plus a linked system user with the given role; both are cleaned up afterwards
     * (user first, then employee).
     */
    public SystemUser seedSystemUser(UserRole role) {
        Employee employee = seedEmployee();
        Credentials credentials = TestDataGenerator.userCredentials(role);
        int userId = api.users().createUser(credentials, role, employee.getEmpNumber())
                .jsonPath().getInt("data.id");
        cleanup.register("Delete system user " + credentials.username(),
                () -> api.users().deleteUsers(List.of(userId)));
        LOG.info("Seeded {} user '{}' for {}", role.label(), credentials.username(), employee);
        return new SystemUser(userId, role, credentials, employee);
    }

    /**
     * Runs all registered cleanup actions. Never throws.
     */
    public List<String> cleanup() {
        if (cleanup.isEmpty()) {
            return List.of();
        }
        List<String> failures = cleanup.runAll();
        if (!failures.isEmpty()) {
            LOG.warn("{} cleanup action(s) failed; data may need manual removal: {}", failures.size(), failures);
        }
        return failures;
    }
}
