package com.lifepulse.controller;

import com.lifepulse.dto.VehicleExpenseSummaryResponse;
import com.lifepulse.dto.VehicleRequest;
import com.lifepulse.dto.VehicleResponse;
import com.lifepulse.repository.FuelLogRepository;
import com.lifepulse.repository.MaintenanceLogRepository;
import com.lifepulse.repository.VehicleRepository;
import com.lifepulse.security.UserDetailsImpl;
import com.lifepulse.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAll(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(vehicleService.getAllByUserId(userDetails.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicle(@PathVariable UUID id) {
        return ResponseEntity.ok(vehicleService.getVehicle(id));
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> create(
            @Valid @RequestBody VehicleRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(vehicleService.create(request, userDetails.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(vehicleService.updateVehicle(userDetails.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        vehicleService.deleteVehicle(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/expense-summary")
    public ResponseEntity<VehicleExpenseSummaryResponse> getExpenseSummary(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UUID userId = userDetails.getId();
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
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @RequestBody Map<String, Integer> body) {
        int currentOdometer = body.getOrDefault("currentOdometer", 0);
        return ResponseEntity.ok(vehicleService.resetMaintenance(userDetails.getId(), id, currentOdometer));
    }
}
