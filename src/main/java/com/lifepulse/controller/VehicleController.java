package com.lifepulse.controller;

import com.lifepulse.dto.VehicleExpenseSummaryResponse;
import com.lifepulse.dto.VehicleRequest;
import com.lifepulse.dto.VehicleResponse;
import com.lifepulse.repository.FuelLogRepository;
import com.lifepulse.repository.MaintenanceLogRepository;
import com.lifepulse.repository.VehicleRepository;
import com.lifepulse.service.CurrentUserService;
import com.lifepulse.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final FuelLogRepository fuelLogRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final VehicleRepository vehicleRepository;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAll(Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(vehicleService.getAllByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicle(@PathVariable UUID id) {
        return ResponseEntity.ok(vehicleService.getVehicle(id));
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> create(
            @Valid @RequestBody VehicleRequest request,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(vehicleService.create(request, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody VehicleRequest request) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(vehicleService.updateVehicle(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(
            Authentication authentication,
            @PathVariable UUID id) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        vehicleService.deleteVehicle(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/expense-summary")
    public ResponseEntity<VehicleExpenseSummaryResponse> getExpenseSummary(
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        double fuelCost = fuelLogRepository.sumTotalCostByOwnerId(userId);
        double maintenanceCost = maintenanceLogRepository.sumCostByOwnerId(userId);
        int vehicleCount = vehicleRepository.findByUserId(userId).size();
        return ResponseEntity.ok(VehicleExpenseSummaryResponse.builder()
                .totalFuelCost(fuelCost)
                .totalMaintenanceCost(maintenanceCost)
                .totalVehicleCost(fuelCost + maintenanceCost)
                .ownedVehicleCount(vehicleCount)
                .build());
    }

    @PostMapping("/{id}/reset-maintenance")
    public ResponseEntity<VehicleResponse> resetMaintenance(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody Map<String, Integer> body) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        int currentOdometer = body.getOrDefault("currentOdometer", 0);
        return ResponseEntity.ok(vehicleService.resetMaintenance(userId, id, currentOdometer));
    }
}
