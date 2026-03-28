package com.vms.controller;

import com.vms.dto.MeterAccessResponse;
import com.vms.security.UserDetailsImpl;
import com.vms.service.MeterAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/meters/access")
@RequiredArgsConstructor
public class MeterAccessController {

    private final MeterAccessService meterAccessService;

    @PostMapping("/request")
    public ResponseEntity<MeterAccessResponse> requestAccess(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String identifier = request.get("identifier");
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Identifier is required");
        }
        return ResponseEntity.ok(meterAccessService.requestAccess(identifier, userDetails.getId()));
    }

    @PostMapping("/invite")
    public ResponseEntity<MeterAccessResponse> inviteUser(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String email = request.get("email");
        String meterIdStr = request.get("meterId");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (meterIdStr == null || meterIdStr.isBlank()) {
            throw new IllegalArgumentException("MeterId is required");
        }

        UUID meterId = UUID.fromString(meterIdStr);
        return ResponseEntity.ok(meterAccessService.inviteUserByEmail(meterId, email, userDetails.getId()));
    }
}
