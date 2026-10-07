package com.orangehrm.api;

import com.framework.api.ApiClient;
import com.framework.api.AuthStrategy;
import com.orangehrm.models.Credentials;
import com.orangehrm.models.UserRole;

import io.restassured.response.Response;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Client for the Admin &gt; System Users endpoints, used to create role-specific accounts on demand.
 */
public class UserApiClient extends ApiClient {

    public UserApiClient(String baseUri, AuthStrategy authStrategy) {
        super(baseUri, authStrategy);
    }

    /**
     * Creates an enabled system user with the given role for an existing employee.
     */
    public Response createUser(Credentials credentials, UserRole role, int empNumber) {
        Map<String, Object> body = Map.of(
                "username", credentials.username(),
                "password", credentials.password(),
                "status", true,
                "userRoleId", role.roleId(),
                "empNumber", empNumber);
        return expectSuccess(request().body(body).post(ApiEndpoints.SYSTEM_USERS), "Create system user");
    }

    /**
     * Deletes the given system users.
     */
    public Response deleteUsers(Collection<Integer> userIds) {
        return expectSuccess(request().body(Map.of("ids", List.copyOf(userIds))).delete(ApiEndpoints.SYSTEM_USERS),
                "Delete system users");
    }
}
