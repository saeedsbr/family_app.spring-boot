package com.lifepulse.service;

import com.lifepulse.dto.MaintenanceStatus;
import com.lifepulse.dto.VehicleRequest;
import com.lifepulse.dto.VehicleResponse;
import com.lifepulse.entity.User;
import com.lifepulse.entity.Vehicle;
import com.lifepulse.entity.VehicleAccess;
import com.lifepulse.repository.UserRepository;
import com.lifepulse.repository.VehicleAccessRepository;
import com.lifepulse.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleAccessRepository vehicleAccessRepository;
    private final UserRepository userRepository;
    private final MaintenanceService maintenanceService;

    /**
     * Returns owned vehicles + shared vehicles the user has been approved for.
     */
    public List<VehicleResponse> getAllByUserId(UUID userId) {
        List<Vehicle> owned = vehicleRepository.findByUserId(userId);

        List<Vehicle> shared = vehicleAccessRepository
                .findByUserIdAndAccessStatus(userId, VehicleAccess.AccessStatus.APPROVED)
                .stream()
                .map(VehicleAccess::getVehicle)
                .collect(Collectors.toList());

        // Merge and deduplicate
        List<Vehicle> all = Stream.concat(owned.stream(), shared.stream())
                .distinct()
                .collect(Collectors.toList());

        return all.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public VehicleResponse getVehicle(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        return mapToResponse(vehicle);
    }

    @Transactional
    public VehicleResponse create(VehicleRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Vehicle vehicle = Vehicle.builder()
                .name(request.getName())
                .brand(request.getBrand())
                .model(request.getModel())
                .modelYear(request.getModelYear())
                .licensePlate(request.getLicensePlate() != null
                        ? request.getLicensePlate().trim().toUpperCase()
                        : null)
                .initialOdometer(request.getInitialOdometer())
                .currentOdometer(request.getInitialOdometer())
                .lastServiceOdometer(request.getInitialOdometer() != null ? request.getInitialOdometer() : 0)
                .currency(request.getCurrency() != null ? request.getCurrency() : "$")
                .user(user)
                .build();

        vehicle = vehicleRepository.save(vehicle);
        return mapToResponse(vehicle);
    }

    /**
     * Only the owner can update the vehicle.
     */
    @Transactional
    public VehicleResponse updateVehicle(UUID userId, UUID vehicleId, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicle.getUser().getId().equals(userId)) {
            throw new RuntimeException("Only the vehicle owner can update this vehicle");
        }

        vehicle.setName(request.getName());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setModelYear(request.getModelYear());
        if (request.getLicensePlate() != null) {
            vehicle.setLicensePlate(request.getLicensePlate().trim().toUpperCase());
        }
        if (request.getInitialOdometer() != null) {
            vehicle.setCurrentOdometer(request.getInitialOdometer());
        }

        vehicle = vehicleRepository.save(vehicle);
        return mapToResponse(vehicle);
    }

    /**
     * Only the owner can delete the vehicle.
     */
    @Transactional
    public void deleteVehicle(UUID userId, UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicle.getUser().getId().equals(userId)) {
            throw new RuntimeException("Only the vehicle owner can delete this vehicle");
        }

        vehicleRepository.delete(vehicle);
    }

    /**
     * Reset maintenance counter for a vehicle (owner only).
     */
    @Transactional
    public VehicleResponse resetMaintenance(UUID userId, UUID vehicleId, int currentOdometer) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicle.getUser().getId().equals(userId)) {
            throw new RuntimeException("Only the vehicle owner can reset maintenance");
        }

        vehicle.setLastServiceOdometer(currentOdometer);
        vehicle.setCurrentOdometer(Math.max(currentOdometer, vehicle.getCurrentOdometer()));
        vehicle = vehicleRepository.save(vehicle);
        return mapToResponse(vehicle);
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        MaintenanceStatus status = maintenanceService.calculateMaintenanceStatus(vehicle);
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .name(vehicle.getName())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .modelYear(vehicle.getModelYear())
                .licensePlate(vehicle.getLicensePlate())
                .currentOdometer(vehicle.getCurrentOdometer())
                .lastServiceOdometer(vehicle.getLastServiceOdometer())
                .currency(vehicle.getCurrency())
                .maintenanceStatus(status)
                .build();
    }
}
