package com.lifepulse.controller;

import com.lifepulse.dto.VehicleAccessRequestDTO;
import com.lifepulse.dto.VehicleAccessResponse;
import com.lifepulse.service.CurrentUserService;
import com.lifepulse.service.VehicleAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles/access")
@RequiredArgsConstructor
public class VehicleAccessController {

    private final VehicleAccessService vehicleAccessService;
    private final CurrentUserService currentUserService;

    @PostMapping("/request")
    public ResponseEntity<VehicleAccessResponse> requestAccess(
            Authentication authentication,
            @RequestBody VehicleAccessRequestDTO request) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(vehicleAccessService.requestAccess(userId, request.getLicensePlate()));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<VehicleAccessResponse>> getPendingRequests(
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(vehicleAccessService.getPendingRequestsForOwner(userId));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<VehicleAccessResponse> approveRequest(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(vehicleAccessService.approveRequest(userId, id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<VehicleAccessResponse> rejectRequest(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(vehicleAccessService.rejectRequest(userId, id));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<VehicleAccessResponse>> getMyRequests(
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(vehicleAccessService.getMyRequests(userId));
    }

    // NEW: Owner invites a user by email to access their vehicle
    @PostMapping("/invite")
    public ResponseEntity<VehicleAccessResponse> inviteUser(
            Authentication authentication,
            @RequestBody Map<String, String> body) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        String email = body.get("email");
        String vehicleId = body.get("vehicleId");
        return ResponseEntity.ok(vehicleAccessService.inviteUserByEmail(
                userId, email, UUID.fromString(vehicleId)));
    }
}
