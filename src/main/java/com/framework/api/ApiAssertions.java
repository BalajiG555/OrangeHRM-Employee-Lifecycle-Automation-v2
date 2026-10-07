package com.framework.api;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.Assert;

/**
 * Reusable API assertions (status codes and JSON-schema contract checks).
 */
public final class ApiAssertions {

    private ApiAssertions() {
    }

    /**
     * Asserts the HTTP status code, including the response body in the failure message.
     */
    public static void assertStatus(Response response, int expectedStatus) {
        Assert.assertEquals(response.statusCode(), expectedStatus,
                "Unexpected HTTP status. Body: " + response.asString());
    }

    /**
     * Validates the response body against a JSON schema on the classpath (e.g. {@code schemas/x.json}).
     */
    public static void assertMatchesSchema(Response response, String schemaClasspathLocation) {
        response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaClasspathLocation));
    }
}
