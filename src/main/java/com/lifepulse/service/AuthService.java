package com.lifepulse.service;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("Email is already taken!");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .currency(request.getCurrency() != null ? request.getCurrency() : "$")
                .logoUrl(request.getLogoUrl())
                .build();

        user = userRepository.save(user);

        // Auto login after registration
        return login(LoginRequest.builder()
                .email(email)
                .password(request.getPassword())
                .build());
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
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
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
}
