package com.lifepulse.service;

import com.lifepulse.entity.User;
import com.lifepulse.repository.UserRepository;
import com.lifepulse.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Resolves the currently authenticated user from either a local JWT (UserDetailsImpl)
 * or a Keycloak JWT (OAuth2 Resource Server), and looks up the local DB user.
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public UUID getCurrentUserId(Authentication authentication) {
        return getCurrentUser(authentication).getId();
    }

    public User getCurrentUser(Authentication authentication) {
        // Local JWT path — UserDetailsImpl is the principal
        if (authentication.getPrincipal() instanceof UserDetailsImpl ud) {
            return userRepository.findById(ud.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        // Keycloak JWT path — extract email claim
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            if (email != null) {
                return userRepository.findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
            }
        }

        // Fallback: try by authentication name (subject)
        String name = authentication.getName();
        return userRepository.findByEmailIgnoreCase(name)
                .orElseThrow(() -> new RuntimeException("User not found: " + name));
    }
}
