package com.lifepulse.controller;

import com.lifepulse.dto.FuelLogRequest;
import com.lifepulse.dto.FuelLogResponse;
import com.lifepulse.security.UserDetailsImpl;
import com.lifepulse.service.FuelLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fuel-logs")
@RequiredArgsConstructor
public class FuelLogController {

    private final FuelLogService fuelLogService;

    @GetMapping
    public ResponseEntity<List<FuelLogResponse>> getLogs(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) UUID vehicleId) {
        return ResponseEntity.ok(fuelLogService.getLogs(userDetails.getId(), vehicleId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<FuelLogResponse>> getRecent(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(fuelLogService.getRecentLogs(userDetails.getId(), limit));
    }

    @PostMapping("/{vehicleId}")
    public ResponseEntity<FuelLogResponse> addLog(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID vehicleId,
            @Valid @RequestBody FuelLogRequest request) {
        return ResponseEntity.ok(fuelLogService.addLog(userDetails.getId(), vehicleId, request));
    }
}
