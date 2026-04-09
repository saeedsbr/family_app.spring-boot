package com.lifepulse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Interacts with the Keycloak Admin API using the family-app-client service account.
 * Same Keycloak realm as sabthings — enables SSO across both apps.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    private final RestClient restClient;

    /**
     * Creates a user in Keycloak. Throws if email already exists (409).
     *
     * @return Keycloak user ID
     */
    public String createKeycloakUser(String email, String password, String name) {
        String adminToken = getAdminToken();

        Map<String, Object> userRep = new java.util.LinkedHashMap<>();
        userRep.put("username", email);
        userRep.put("email", email);
        userRep.put("enabled", true);
        userRep.put("emailVerified", true);
        if (name != null && !name.isBlank()) {
            String[] parts = name.trim().split(" ", 2);
            userRep.put("firstName", parts[0]);
            userRep.put("lastName", parts.length > 1 ? parts[1] : "");
        }

        ResponseEntity<Void> response;
        try {
            response = restClient.post()
                    .uri(usersUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(userRep)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 409) {
                throw new IllegalArgumentException("KEYCLOAK_EXISTS");
            }
            throw new RuntimeException("Keycloak user creation failed: " + e.getMessage());
        }

        String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
        String keycloakUserId = location.substring(location.lastIndexOf('/') + 1);

        setPassword(adminToken, keycloakUserId, password);
        log.info("Created Keycloak user {} with id {}", email, keycloakUserId);
        return keycloakUserId;
    }

    /**
     * Checks whether an email already exists in the Keycloak realm.
     * Returns true if found — meaning the user is already registered (via sabthings or this app).
     */
    public boolean emailExistsInKeycloak(String email) {
        try {
            String adminToken = getAdminToken();
            List<Map<String, Object>> users = restClient.get()
                    .uri(usersUrl() + "?email=" + email + "&exact=true")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return users != null && !users.isEmpty();
        } catch (Exception e) {
            log.error("Failed to check email in Keycloak: {}", e.getMessage());
            return false; // Fail open — don't block registration if Keycloak is temporarily down
        }
    }

    /**
     * Deletes a Keycloak user — used as compensation if local DB creation fails.
     */
    public void deleteKeycloakUser(String keycloakUserId) {
        try {
            restClient.delete()
                    .uri(usersUrl() + "/" + keycloakUserId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAdminToken())
                    .retrieve()
                    .toBodilessEntity();
            log.info("Deleted Keycloak user {}", keycloakUserId);
        } catch (Exception e) {
            log.error("Failed to delete Keycloak user {} (compensation): {}", keycloakUserId, e.getMessage());
        }
    }

    private void setPassword(String adminToken, String userId, String password) {
        restClient.put()
                .uri(usersUrl() + "/" + userId + "/reset-password")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("type", "password", "value", password, "temporary", false))
                .retrieve()
                .toBodilessEntity();
    }

    private String getAdminToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        Map<String, Object> tokenResp = restClient.post()
                .uri(authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        return (String) tokenResp.get("access_token");
    }

    private String usersUrl() {
        return authServerUrl + "/admin/realms/" + realm + "/users";
    }
}
