package com.lifepulse.controller;

import com.lifepulse.dto.VehicleAccessRequestDTO;
import com.lifepulse.dto.VehicleAccessResponse;
import com.lifepulse.security.UserDetailsImpl;
import com.lifepulse.service.VehicleAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles/access")
@RequiredArgsConstructor
public class VehicleAccessController {

    private final VehicleAccessService vehicleAccessService;

    @PostMapping("/request")
    public ResponseEntity<VehicleAccessResponse> requestAccess(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody VehicleAccessRequestDTO request) {
        return ResponseEntity.ok(vehicleAccessService.requestAccess(userDetails.getId(), request.getLicensePlate()));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<VehicleAccessResponse>> getPendingRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(vehicleAccessService.getPendingRequestsForOwner(userDetails.getId()));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<VehicleAccessResponse> approveRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(vehicleAccessService.approveRequest(userDetails.getId(), id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<VehicleAccessResponse> rejectRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(vehicleAccessService.rejectRequest(userDetails.getId(), id));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<VehicleAccessResponse>> getMyRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(vehicleAccessService.getMyRequests(userDetails.getId()));
    }

    // NEW: Owner invites a user by email to access their vehicle
    @PostMapping("/invite")
    public ResponseEntity<VehicleAccessResponse> inviteUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, String> body) {
        String email = body.get("email");
        String vehicleId = body.get("vehicleId");
        return ResponseEntity.ok(vehicleAccessService.inviteUserByEmail(
                userDetails.getId(), email, UUID.fromString(vehicleId)));
    }
}
