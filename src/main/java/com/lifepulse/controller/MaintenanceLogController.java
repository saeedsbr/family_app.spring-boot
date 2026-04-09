package com.lifepulse.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.lifepulse.dto.MaintenanceLogRequest;
import com.lifepulse.dto.MaintenanceLogResponse;
import com.lifepulse.service.CurrentUserService;
import com.lifepulse.service.MaintenanceLogService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/maintenance-logs")
@RequiredArgsConstructor
public class MaintenanceLogController {

    private final MaintenanceLogService maintenanceLogService;
    private final CurrentUserService currentUserService;

    @GetMapping("/activities/recent")
    public ResponseEntity<List<MaintenanceLogResponse>> getRecent(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int limit) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(maintenanceLogService.getRecentLogs(userId, limit));
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<List<MaintenanceLogResponse>> getLogs(
            Authentication authentication,
            @PathVariable UUID vehicleId) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(maintenanceLogService.getLogsByVehicleId(userId, vehicleId));
    }

    @PostMapping("/{vehicleId}")
    public ResponseEntity<MaintenanceLogResponse> addLog(
            Authentication authentication,
            @PathVariable UUID vehicleId,
            @Valid @RequestBody MaintenanceLogRequest request) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(maintenanceLogService.addLog(userId, vehicleId, request));
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(
            Authentication authentication,
            @PathVariable UUID logId) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        maintenanceLogService.deleteLog(userId, logId);
        return ResponseEntity.noContent().build();
    }
}
