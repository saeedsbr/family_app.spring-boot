package com.lifepulse.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifepulse.entity.User;
import com.lifepulse.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ResetController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/api/public/reset-italha")
    public String reset(@RequestParam(defaultValue = "Admin@123") String password) {
        User user = userRepository.findByEmailIgnoreCase("italha.saeedsbr@gmail.com")
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return "Password reset successfully for " + user.getEmail();
    }
}
