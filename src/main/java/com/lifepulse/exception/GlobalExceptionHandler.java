package com.lifepulse.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "A record with this value already exists.";
        String cause = ex.getMostSpecificCause().getMessage();
        if (cause != null) {
            String low = cause.toLowerCase();
            if (low.contains("license_plate") || low.contains("licenseplate")) {
                message = "A vehicle with this license plate already exists.";
            } else if (low.contains("identifier")) {
                message = "This identifier is already taken.";
            } else if (low.contains("email") || low.contains("keycloak") || low.contains("users.uk") || low.contains("uk_") || low.contains("@")) {
                message = "This email is already registered.";
            }
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.CONFLICT.value(),
                "message", message
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", message.isEmpty() ? "Validation failed" : message
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";

        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (message.contains("not found") || message.contains("Not found")) {
            status = HttpStatus.NOT_FOUND;
        } else if (message.contains("already exists") || message.contains("already the owner")) {
            status = HttpStatus.CONFLICT;
        } else if (message.contains("Only the vehicle owner")) {
            status = HttpStatus.FORBIDDEN;
        }

        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "message", message
        ));
    }
}
