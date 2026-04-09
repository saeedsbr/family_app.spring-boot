package com.lifepulse.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifepulse.dto.AuthMeResponse;
import com.lifepulse.dto.AuthResponse;
import com.lifepulse.dto.LoginRequest;
import com.lifepulse.dto.RegisterRequest;
import com.lifepulse.entity.PasswordResetToken;
import com.lifepulse.entity.User;
import com.lifepulse.repository.PasswordResetTokenRepository;
import com.lifepulse.repository.UserRepository;
import com.lifepulse.security.JwtUtils;
import com.lifepulse.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final KeycloakAdminService keycloakAdminService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase();

        // Check if email already exists in Keycloak (registered via sabthings or this app)
        if (keycloakAdminService.emailExistsInKeycloak(email)) {
            throw new IllegalArgumentException("KEYCLOAK_EXISTS");
        }

        // Create user in Keycloak first
        String keycloakUserId = keycloakAdminService.createKeycloakUser(
                email, request.getPassword(), request.getName());

        // Create local user with link to Keycloak identity
        try {
            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) // auth is via Keycloak
                    .name(request.getName())
                    .currency(request.getCurrency() != null ? request.getCurrency() : "$")
                    .logoUrl(request.getLogoUrl())
                    .keycloakId(keycloakUserId)
                    .build();
            user = userRepository.save(user);
            log.info("Registered user {} with keycloakId {}", email, keycloakUserId);

            return AuthResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .build();
        } catch (Exception e) {
            // Compensate: remove from Keycloak to keep both systems consistent
            keycloakAdminService.deleteKeycloakUser(keycloakUserId);
            throw new RuntimeException("Registration failed. Please try again.");
        }
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return AuthResponse.builder()
                .accessToken(jwt)
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .name(userDetails.getName())
                .currency(userDetails.getCurrency())
                .logoUrl(userDetails.getLogoUrl())
                .build();
    }

    public AuthMeResponse getMe(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return AuthMeResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .currency(user.getCurrency())
                .logoUrl(user.getLogoUrl())
                .build();
    }

    @Transactional
    public void forgotPassword(String email) {
        if (email == null)
            throw new RuntimeException("Email is required");
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Delete any existing token for the user
        tokenRepository.deleteByUser(user);

        // Generate new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();

        tokenRepository.save(resetToken);

        // Send email (pointing to frontend URL)
        String resetLink = "http://localhost:3001/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete used token
        tokenRepository.delete(resetToken);
    }

    @Transactional
    public AuthResponse googleLogin(String email, String name) {
        String normalizedEmail = email.toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(normalizedEmail)
                            .name(name)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .currency("$")
                            .build();
                    return userRepository.save(newUser);
                });

        // Generate JWT for the user
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        return AuthResponse.builder()
                .accessToken(jwt)
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .currency(user.getCurrency())
                .logoUrl(user.getLogoUrl())
                .build();
    }
}
