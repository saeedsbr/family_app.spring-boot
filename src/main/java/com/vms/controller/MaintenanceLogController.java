package com.vms.controller;

import com.vms.dto.MaintenanceLogRequest;
import com.vms.dto.MaintenanceLogResponse;
import com.vms.security.UserDetailsImpl;
import com.vms.service.MaintenanceLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/maintenance")
@RequiredArgsConstructor
public class MaintenanceLogController {

    private final MaintenanceLogService maintenanceLogService;

    @GetMapping("/activities/recent")
    public ResponseEntity<List<MaintenanceLogResponse>> getRecent(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(maintenanceLogService.getRecentLogs(userDetails.getId(), limit));
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<List<MaintenanceLogResponse>> getLogs(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID vehicleId) {
        return ResponseEntity.ok(maintenanceLogService.getLogsByVehicleId(userDetails.getId(), vehicleId));
    }

    @PostMapping("/{vehicleId}")
    public ResponseEntity<MaintenanceLogResponse> addLog(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID vehicleId,
            @Valid @RequestBody MaintenanceLogRequest request) {
        return ResponseEntity.ok(maintenanceLogService.addLog(userDetails.getId(), vehicleId, request));
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID logId) {
        maintenanceLogService.deleteLog(userDetails.getId(), logId);
        return ResponseEntity.noContent().build();
    }
}
