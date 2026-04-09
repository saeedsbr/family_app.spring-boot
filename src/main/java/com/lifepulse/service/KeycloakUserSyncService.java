package com.lifepulse.service;

import com.lifepulse.entity.User;
import com.lifepulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Syncs Keycloak-authenticated users into the local lifepulse MySQL database.
 * Called on every Keycloak JWT validation — ensures user always exists locally.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserSyncService {

    private final UserRepository userRepository;

    @Transactional
    public void syncUserFromKeycloak(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        String email = jwt.getClaim("email");
        if (email == null) email = keycloakId;

        // Resolve display name from token claims
        String name = jwt.getClaim("name");
        if (name == null || name.isBlank()) {
            String first = jwt.getClaim("given_name");
            String last = jwt.getClaim("family_name");
            name = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
        }
        if (name.isBlank()) name = email;

        // Try keycloakId first, then email (handles users registered before this integration)
        Optional<User> existing = userRepository.findByKeycloakId(keycloakId);
        if (existing.isEmpty()) {
            existing = userRepository.findByEmailIgnoreCase(email);
        }

        if (existing.isEmpty()) {
            // First-ever Keycloak login — create local record
            User newUser = User.builder()
                    .email(email)
                    .name(name)
                    .password(UUID.randomUUID().toString()) // Not used; auth is via Keycloak
                    .keycloakId(keycloakId)
                    .currency("$")
                    .build();
            userRepository.save(newUser);
            log.info("Created local user for Keycloak subject: {} ({})", keycloakId, email);
        } else {
            User user = existing.get();
            // Link keycloakId if this is an existing user logging in via Keycloak for first time
            if (user.getKeycloakId() == null) {
                user.setKeycloakId(keycloakId);
                userRepository.save(user);
                log.info("Linked existing user {} to Keycloak ID: {}", email, keycloakId);
            }
        }
    }
}
