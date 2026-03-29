package com.lifepulse.service;

import com.lifepulse.dto.MaintenanceLogRequest;
import com.lifepulse.dto.MaintenanceLogResponse;
import com.lifepulse.entity.MaintenanceLog;
import com.lifepulse.entity.Vehicle;
import com.lifepulse.repository.MaintenanceLogRepository;
import com.lifepulse.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceLogService {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleAccessService vehicleAccessService;

    @Transactional(readOnly = true)
    public List<MaintenanceLogResponse> getRecentLogs(UUID userId, int limit) {
        return maintenanceLogRepository
                .findByVehicleUserIdOrderByServiceDateDesc(userId,
                        org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceLogResponse> getLogsByVehicleId(UUID userId, UUID vehicleId) {
        if (!vehicleAccessService.canUserAccessVehicle(userId, vehicleId)) {
            throw new RuntimeException("You do not have permission to view logs for this vehicle");
        }
        return maintenanceLogRepository.findByVehicleIdOrderByServiceDateDesc(vehicleId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MaintenanceLogResponse addLog(UUID userId, UUID vehicleId, MaintenanceLogRequest request) {
        if (!vehicleAccessService.canUserAccessVehicle(userId, vehicleId)) {
            throw new RuntimeException("You do not have permission to add logs for this vehicle");
        }
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        MaintenanceLog log = MaintenanceLog.builder()
                .vehicle(vehicle)
                .serviceName(request.getServiceName())
                .description(request.getDescription())
                .type(request.getType())
                .odometer(request.getOdometer())
                .cost(request.getCost())
                .serviceDate(request.getServiceDate())
                .build();

        log = maintenanceLogRepository.save(log);

        // Update vehicle's last service odometer if this is the most recent
        if (vehicle.getLastServiceOdometer() == null || log.getOdometer() > vehicle.getLastServiceOdometer()) {
            vehicle.setLastServiceOdometer(log.getOdometer());
            vehicleRepository.save(vehicle);
        }

        return mapToResponse(log);
    }

    @Transactional
    public void deleteLog(UUID userId, UUID logId) {
        MaintenanceLog log = maintenanceLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Maintenance log not found"));

        if (!vehicleAccessService.canUserAccessVehicle(userId, log.getVehicle().getId())) {
            throw new RuntimeException("You do not have permission to delete this log");
        }

        maintenanceLogRepository.deleteById(logId);
    }

    private MaintenanceLogResponse mapToResponse(MaintenanceLog log) {
        return MaintenanceLogResponse.builder()
                .id(log.getId())
                .vehicleId(log.getVehicle().getId())
                .serviceName(log.getServiceName())
                .description(log.getDescription())
                .type(log.getType())
                .odometer(log.getOdometer())
                .cost(log.getCost())
                .serviceDate(log.getServiceDate())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
