package com.lifepulse.controller;

import com.lifepulse.dto.FuelLogRequest;
import com.lifepulse.dto.FuelLogResponse;
import com.lifepulse.service.CurrentUserService;
import com.lifepulse.service.FuelLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fuel-logs")
@RequiredArgsConstructor
public class FuelLogController {

    private final FuelLogService fuelLogService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<FuelLogResponse>> getLogs(
            Authentication authentication,
            @RequestParam(required = false) UUID vehicleId) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(fuelLogService.getLogs(userId, vehicleId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<FuelLogResponse>> getRecent(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int limit) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(fuelLogService.getRecentLogs(userId, limit));
    }

    @PostMapping("/{vehicleId}")
    public ResponseEntity<FuelLogResponse> addLog(
            Authentication authentication,
            @PathVariable UUID vehicleId,
            @Valid @RequestBody FuelLogRequest request) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(fuelLogService.addLog(userId, vehicleId, request));
    }
}
