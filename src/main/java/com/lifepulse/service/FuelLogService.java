package com.lifepulse.service;

import com.lifepulse.dto.FuelLogRequest;
import com.lifepulse.dto.FuelLogResponse;
import com.lifepulse.entity.FuelLog;
import com.lifepulse.entity.Vehicle;
import com.lifepulse.entity.VehicleAccess;
import com.lifepulse.repository.FuelLogRepository;
import com.lifepulse.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuelLogService {

    private final FuelLogRepository fuelLogRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleAccessService vehicleAccessService;

    /**
     * Get fuel logs, optionally filtered by vehicleId.
     * If vehicleId is null, returns all logs for user's owned + shared vehicles.
     */
    public List<FuelLogResponse> getLogs(UUID userId, UUID vehicleId) {
        if (vehicleId != null) {
            if (!vehicleAccessService.canUserAccessVehicle(userId, vehicleId)) {
                throw new RuntimeException("You do not have permission to view logs for this vehicle");
            }
            return fuelLogRepository.findByVehicleIdOrderByLogDateDesc(vehicleId)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        // Return all logs user can access
        return fuelLogRepository.findRecentLogsByUserId(userId, VehicleAccess.AccessStatus.APPROVED)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get recent fuel logs across all accessible vehicles.
     */
    public List<FuelLogResponse> getRecentLogs(UUID userId, int limit) {
        List<FuelLog> logs = fuelLogRepository.findRecentLogsByUserId(userId, VehicleAccess.AccessStatus.APPROVED);
        return logs.stream()
                .limit(limit)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Add a fuel log. Only owner or approved users can add.
     */
    @Transactional
    public FuelLogResponse addLog(UUID userId, UUID vehicleId, FuelLogRequest request) {
        if (!vehicleAccessService.canUserAccessVehicle(userId, vehicleId)) {
            throw new RuntimeException("You do not have permission to add logs for this vehicle");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        FuelLog log = FuelLog.builder()
                .odometer(request.getOdometer())
                .fuelAmount(request.getFuelAmount())
                .totalCost(request.getTotalCost())
                .logDate(request.getLogDate() != null ? request.getLogDate() : LocalDateTime.now())
                .vehicle(vehicle)
                .build();

        // Calculate fuel economy
        Double fuelEconomy = null;
        if (log.getFuelAmount() != null && log.getFuelAmount() > 0) {
            Integer previousOdometer = fuelLogRepository.findFirstByVehicleIdOrderByLogDateDesc(vehicleId)
                    .map(FuelLog::getOdometer)
                    .orElse(vehicle.getInitialOdometer());

            if (previousOdometer != null && log.getOdometer() > previousOdometer) {
                double distance = log.getOdometer() - previousOdometer;
                fuelEconomy = distance / log.getFuelAmount();
            }
        }
        log.setFuelEconomy(fuelEconomy);

        // Update vehicle odometer if new reading is higher
        if (request.getOdometer() != null
                && (vehicle.getCurrentOdometer() == null || request.getOdometer() > vehicle.getCurrentOdometer())) {
            vehicle.setCurrentOdometer(request.getOdometer());
            vehicleRepository.save(vehicle);
        }

        log = fuelLogRepository.save(log);
        return mapToResponse(log);
    }

    private FuelLogResponse mapToResponse(FuelLog log) {
        return FuelLogResponse.builder()
                .id(log.getId())
                .vehicleId(log.getVehicle().getId())
                .odometer(log.getOdometer())
                .fuelAmount(log.getFuelAmount())
                .totalCost(log.getTotalCost())
                .logDate(log.getLogDate() != null ? log.getLogDate().toString() : null)
                .fuelEconomy(log.getFuelEconomy())
                .createdAt(log.getCreatedAt() != null ? log.getCreatedAt().toString() : null)
                .build();
    }
}
