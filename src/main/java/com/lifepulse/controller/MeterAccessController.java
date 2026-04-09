package com.lifepulse.controller;

import com.lifepulse.dto.MeterAccessResponse;
import com.lifepulse.service.CurrentUserService;
import com.lifepulse.service.MeterAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/meters/access")
@RequiredArgsConstructor
public class MeterAccessController {

    private final MeterAccessService meterAccessService;
    private final CurrentUserService currentUserService;

    @PostMapping("/request")
    public ResponseEntity<MeterAccessResponse> requestAccess(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        String identifier = request.get("identifier");
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Identifier is required");
        }
        return ResponseEntity.ok(meterAccessService.requestAccess(identifier, userId));
    }

    @PostMapping("/invite")
    public ResponseEntity<MeterAccessResponse> inviteUser(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        String email = request.get("email");
        String meterIdStr = request.get("meterId");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (meterIdStr == null || meterIdStr.isBlank()) {
            throw new IllegalArgumentException("MeterId is required");
        }

        UUID meterId = UUID.fromString(meterIdStr);
        return ResponseEntity.ok(meterAccessService.inviteUserByEmail(meterId, email, userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<MeterAccessResponse>> getPendingRequests(
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(meterAccessService.getPendingRequestsForOwner(userId));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<MeterAccessResponse> approveRequest(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(meterAccessService.approveRequest(userId, id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<MeterAccessResponse> rejectRequest(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(meterAccessService.rejectRequest(userId, id));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<MeterAccessResponse>> getMyRequests(
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(meterAccessService.getMyRequests(userId));
    }

    @GetMapping("/{meterId}/members")
    public ResponseEntity<List<MeterAccessResponse>> getMeterMembers(
            Authentication authentication,
            @PathVariable UUID meterId) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(meterAccessService.getMeterMembers(meterId, userId));
    }
}
