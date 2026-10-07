package com.framework.unit;

import com.orangehrm.data.TestDataGenerator;
import com.orangehrm.models.Credentials;
import com.orangehrm.models.Employee;
import com.orangehrm.models.UserRole;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * Test-data generation must be unique under parallel execution and respect OrangeHRM's field rules.
 */
public class TestDataGeneratorTest {

    private static final int GENERATED = 2_000;
    private static final int EMPLOYEE_ID_MAX_LENGTH = 10;

    @Test
    public void employeeIdsAreUniqueWhenGeneratedInParallel() {
        Set<String> ids = ConcurrentHashMap.newKeySet();
        IntStream.range(0, GENERATED).parallel().forEach(i -> ids.add(TestDataGenerator.employee().getEmployeeId()));
        Assert.assertEquals(ids.size(), GENERATED, "Every generated Employee Id must be unique");
    }

    @Test
    public void employeeRespectsTemplateAndFieldLimits() {
        Employee employee = TestDataGenerator.employee();
        Assert.assertTrue(employee.getEmployeeId().length() <= EMPLOYEE_ID_MAX_LENGTH, "Employee Id length");
        Assert.assertTrue(employee.getFirstName().startsWith("Automation"), "First name comes from the template");
        Assert.assertEquals(employee.getJobTitle(), "QA Engineer");
        Assert.assertEquals(employee.getProfilePicture(), "testdata/profile.png");
        Assert.assertNull(employee.getEmpNumber(), "empNumber is only known after creation");
    }

    @Test
    public void generatedPasswordsSatisfyComplexityRules() {
        List<Credentials> credentials = IntStream.range(0, 200)
                .mapToObj(i -> TestDataGenerator.userCredentials(UserRole.ESS)).toList();
        for (Credentials c : credentials) {
            String p = c.password();
            Assert.assertTrue(p.chars().anyMatch(Character::isUpperCase), "upper case: " + p);
            Assert.assertTrue(p.chars().anyMatch(Character::isLowerCase), "lower case: " + p);
            Assert.assertTrue(p.chars().anyMatch(Character::isDigit), "digit: " + p);
            Assert.assertTrue(c.username().startsWith("ess_"), "username prefix: " + c.username());
        }
    }

    @Test
    public void credentialsNeverPrintPasswords() {
        Credentials credentials = new Credentials("user", "secret-value");
        Assert.assertFalse(credentials.toString().contains("secret-value"));
    }
}
