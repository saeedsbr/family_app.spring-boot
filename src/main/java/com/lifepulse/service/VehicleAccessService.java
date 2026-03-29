package com.lifepulse.service;

import com.lifepulse.dto.VehicleAccessResponse;
import com.lifepulse.entity.User;
import com.lifepulse.entity.Vehicle;
import com.lifepulse.entity.VehicleAccess;
import com.lifepulse.repository.UserRepository;
import com.lifepulse.repository.VehicleAccessRepository;
import com.lifepulse.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleAccessService {

    private final VehicleAccessRepository vehicleAccessRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    /**
     * User requests access to a vehicle by license plate.
     */
    @Transactional
    public VehicleAccessResponse requestAccess(UUID userId, String licensePlate) {
        String normalizedPlate = licensePlate.trim().toUpperCase();
        Vehicle vehicle = vehicleRepository.findByLicensePlate(normalizedPlate)
                .orElseThrow(
                        () -> new RuntimeException("Vehicle with license plate " + normalizedPlate + " not found"));

        if (vehicle.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are already the owner of this vehicle");
        }

        vehicleAccessRepository.findByUserIdAndVehicleId(userId, vehicle.getId())
                .ifPresent(access -> {
                    throw new RuntimeException(
                            "Access request already exists with status: " + access.getAccessStatus());
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        VehicleAccess access = VehicleAccess.builder()
                .user(user)
                .vehicle(vehicle)
                .accessStatus(VehicleAccess.AccessStatus.PENDING)
                .build();

        return mapToResponse(vehicleAccessRepository.save(access));
    }

    /**
     * Owner invites a user by email to access their vehicle.
     * Creates an APPROVED access record directly.
     */
    @Transactional
    public VehicleAccessResponse inviteUserByEmail(UUID ownerId, String email, UUID vehicleId) {
        String normalizedEmail = email.trim().toLowerCase();
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicle.getUser().getId().equals(ownerId)) {
            throw new RuntimeException("Only the vehicle owner can invite users");
        }

        User invitedUser = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("No user found with email: " + normalizedEmail));

        if (invitedUser.getId().equals(ownerId)) {
            throw new RuntimeException("You cannot invite yourself");
        }

        // Check if access already exists
        vehicleAccessRepository.findByUserIdAndVehicleId(invitedUser.getId(), vehicleId)
                .ifPresent(access -> {
                    throw new RuntimeException(
                            "Access already exists for this user with status: " + access.getAccessStatus());
                });

        VehicleAccess access = VehicleAccess.builder()
                .user(invitedUser)
                .vehicle(vehicle)
                .accessStatus(VehicleAccess.AccessStatus.APPROVED) // Direct approval by owner
                .build();

        return mapToResponse(vehicleAccessRepository.save(access));
    }

    @Transactional
    public VehicleAccessResponse approveRequest(UUID ownerId, UUID requestId) {
        VehicleAccess access = vehicleAccessRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Access request not found"));

        if (!access.getVehicle().getUser().getId().equals(ownerId)) {
            throw new RuntimeException("Only the vehicle owner can approve access requests");
        }

        access.setAccessStatus(VehicleAccess.AccessStatus.APPROVED);
        return mapToResponse(vehicleAccessRepository.save(access));
    }

    @Transactional
    public VehicleAccessResponse rejectRequest(UUID ownerId, UUID requestId) {
        VehicleAccess access = vehicleAccessRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Access request not found"));

        if (!access.getVehicle().getUser().getId().equals(ownerId)) {
            throw new RuntimeException("Only the vehicle owner can reject access requests");
        }

        access.setAccessStatus(VehicleAccess.AccessStatus.REJECTED);
        return mapToResponse(vehicleAccessRepository.save(access));
    }

    public List<VehicleAccessResponse> getPendingRequestsForOwner(UUID ownerId) {
        return vehicleAccessRepository.findByVehicleUserIdAndAccessStatus(ownerId, VehicleAccess.AccessStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<VehicleAccessResponse> getMyRequests(UUID userId) {
        return vehicleAccessRepository.findByUserIdAndAccessStatus(userId, VehicleAccess.AccessStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Check if a user has access to a vehicle (owner or approved collaborator).
     */
    public boolean canUserAccessVehicle(UUID userId, UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle == null)
            return false;

        // Owner has access
        if (vehicle.getUser().getId().equals(userId))
            return true;

        // Approved collaborators have access
        return vehicleAccessRepository.findByUserIdAndVehicleId(userId, vehicleId)
                .map(access -> access.getAccessStatus() == VehicleAccess.AccessStatus.APPROVED)
                .orElse(false);
    }

    private VehicleAccessResponse mapToResponse(VehicleAccess access) {
        return VehicleAccessResponse.builder()
                .id(access.getId())
                .userName(access.getUser().getName())
                .userEmail(access.getUser().getEmail())
                .vehicleId(access.getVehicle().getId())
                .vehicleName(access.getVehicle().getName())
                .vehicleLicensePlate(access.getVehicle().getLicensePlate())
                .status(access.getAccessStatus())
                .createdAt(access.getCreatedAt())
                .build();
    }
}
