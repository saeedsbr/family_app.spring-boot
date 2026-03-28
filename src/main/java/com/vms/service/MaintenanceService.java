package com.vms.service;

import com.vms.dto.MaintenanceStatus;
import com.vms.dto.MaintenanceStatusType;
import com.vms.entity.Vehicle;
import com.vms.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MaintenanceService {

    private static final int SERVICE_INTERVAL_KM = 5000;
    private static final int WARNING_THRESHOLD_KM = 300;

    private final VehicleRepository vehicleRepository;

    public MaintenanceStatus calculateMaintenanceStatus(Vehicle vehicle) {
        if (vehicle.getLastServiceOdometer() == null) {
            vehicle.setLastServiceOdometer(0);
        }
        if (vehicle.getCurrentOdometer() == null) {
            vehicle.setCurrentOdometer(0);
        }

        int lastService = vehicle.getLastServiceOdometer();
        int currentOdometer = vehicle.getCurrentOdometer();

        int kmSinceLastService = currentOdometer - lastService;
        int kmRemaining = SERVICE_INTERVAL_KM - kmSinceLastService;

        // Calculate progress percentage
        double progressPercentage = (double) kmSinceLastService / SERVICE_INTERVAL_KM * 100;

        // Determine status
        MaintenanceStatusType status;
        if (kmRemaining <= 0) {
            status = MaintenanceStatusType.OVERDUE;
        } else if (kmRemaining <= WARNING_THRESHOLD_KM) {
            status = MaintenanceStatusType.DUE_SOON;
        } else {
            status = MaintenanceStatusType.HEALTHY;
        }

        return MaintenanceStatus.builder()
                .kmRemaining(Math.max(kmRemaining, 0))
                .kmSinceLastService(kmSinceLastService)
                .progressPercentage(Math.min(progressPercentage, 100.0))
                .status(status)
                .nextServiceAt(lastService + SERVICE_INTERVAL_KM)
                .build();
    }

    public void resetMaintenanceCounter(UUID vehicleId, int currentOdometer) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        vehicle.setLastServiceOdometer(currentOdometer);
        vehicle.setUpdatedAt(LocalDateTime.now());

        vehicleRepository.save(vehicle);
        log.info("Maintenance counter reset for vehicle: {}", vehicleId);
    }
}
