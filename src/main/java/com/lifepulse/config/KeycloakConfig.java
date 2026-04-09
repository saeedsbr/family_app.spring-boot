package com.lifepulse.config;

import com.lifepulse.service.KeycloakUserSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Keycloak OAuth2 Resource Server configuration.
 * Validates Keycloak JWT tokens and syncs users to the local database.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class KeycloakConfig {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    private final KeycloakUserSyncService userSyncService;

    /**
     * JwtDecoder that validates Keycloak tokens using JWKS URI.
     * No network call at startup — JWKS is fetched lazily on first token validation.
     */
    @Bean
    JwtDecoder jwtDecoder() {
        String jwksUri = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
        log.info("Configuring Keycloak JwtDecoder with JWKS URI: {}", jwksUri);
        return NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
    }

    /**
     * Converts Keycloak JWT to Spring Security authentication.
     * Also syncs the authenticated user to the local MySQL database.
     */
    @Bean
    JwtAuthenticationConverter keycloakJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakGrantedAuthoritiesConverter());
        return converter;
    }

    /**
     * Extracts authorities from Keycloak JWT (realm roles + client roles)
     * and triggers user sync to local DB.
     */
    private class KeycloakGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            List<GrantedAuthority> authorities = new ArrayList<>();

            Collection<GrantedAuthority> defaults = defaultConverter.convert(jwt);
            if (defaults != null) authorities.addAll(defaults);

            // Realm-level roles
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof Map && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                        .forEach(authorities::add);
            }

            // Client-level roles
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                resourceAccess.values().forEach(v -> {
                    if (v instanceof Map) {
                        @SuppressWarnings("unchecked")
                        List<String> clientRoles = (List<String>) ((Map<?, ?>) v).get("roles");
                        if (clientRoles != null) {
                            clientRoles.stream()
                                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                                    .forEach(authorities::add);
                        }
                    }
                });
            }

            // Sync user to local DB on every Keycloak authentication
            String email = jwt.getClaim("email");
            if (email != null) {
                userSyncService.syncUserFromKeycloak(jwt);
            }

            return authorities;
        }
    }
}
