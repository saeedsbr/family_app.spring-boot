package com.vms.service;

import com.vms.dto.MeterAccessResponse;
import com.vms.entity.Meter;
import com.vms.entity.MeterAccess;
import com.vms.entity.User;
import com.vms.repository.MeterAccessRepository;
import com.vms.repository.MeterRepository;
import com.vms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeterAccessService {

    private final MeterAccessRepository meterAccessRepository;
    private final MeterRepository meterRepository;
    private final UserRepository userRepository;

    @Transactional
    public MeterAccessResponse requestAccess(String identifier, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Meter meter = meterRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new RuntimeException("Meter not found with provided identifier"));

        if (meter.getOwner().getId().equals(userId)) {
            throw new RuntimeException("You are already the owner of this meter");
        }

        if (meterAccessRepository.existsByMeterAndUser(meter, user)) {
            throw new RuntimeException("Access request already exists or you already have access");
        }

        MeterAccess accessRequest = MeterAccess.builder()
                .meter(meter)
                .user(user)
                .accessStatus(MeterAccess.AccessStatus.PENDING)
                .build();

        accessRequest = meterAccessRepository.save(accessRequest);
        return mapToResponse(accessRequest);
    }

    @Transactional
    public MeterAccessResponse inviteUserByEmail(UUID meterId, String email, UUID ownerId) {
        String normalizedEmail = email.trim().toLowerCase();
        Meter meter = meterRepository.findById(meterId)
                .orElseThrow(() -> new RuntimeException("Meter not found"));

        if (!meter.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Only the meter owner can invite users");
        }

        User invitee = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("User with this email not found"));

        if (invitee.getId().equals(ownerId)) {
            throw new RuntimeException("You cannot invite yourself");
        }

        if (meterAccessRepository.existsByMeterAndUser(meter, invitee)) {
            throw new RuntimeException("User already has access or a pending request");
        }

        MeterAccess access = MeterAccess.builder()
                .meter(meter)
                .user(invitee)
                .accessStatus(MeterAccess.AccessStatus.APPROVED) // Auto approved since owner initiated
                .build();

        access = meterAccessRepository.save(access);
        return mapToResponse(access);
    }

    @Transactional
    public MeterAccessResponse approveRequest(UUID ownerId, UUID requestId) {
        MeterAccess access = meterAccessRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Access request not found"));

        if (!access.getMeter().getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Only the meter owner can approve access requests");
        }

        access.setAccessStatus(MeterAccess.AccessStatus.APPROVED);
        return mapToResponse(meterAccessRepository.save(access));
    }

    @Transactional
    public MeterAccessResponse rejectRequest(UUID ownerId, UUID requestId) {
        MeterAccess access = meterAccessRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Access request not found"));

        if (!access.getMeter().getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Only the meter owner can reject access requests");
        }

        access.setAccessStatus(MeterAccess.AccessStatus.REJECTED);
        return mapToResponse(meterAccessRepository.save(access));
    }

    public List<MeterAccessResponse> getPendingRequestsForOwner(UUID ownerId) {
        return meterAccessRepository.findByMeterOwnerIdAndAccessStatus(ownerId, MeterAccess.AccessStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MeterAccessResponse> getMyRequests(UUID userId) {
        return meterAccessRepository.findByUserAndAccessStatus(
                userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")),
                MeterAccess.AccessStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public boolean canUserAccessMeter(UUID userId, UUID meterId) {
        Meter meter = meterRepository.findById(meterId).orElse(null);
        if (meter == null)
            return false;

        if (meter.getOwner().getId().equals(userId))
            return true;

        return meterAccessRepository.findByUserIdAndMeterId(userId, meterId)
                .map(access -> access.getAccessStatus() == MeterAccess.AccessStatus.APPROVED)
                .orElse(false);
    }

    // Leveraging existing VehicleAccessResponse DTO for standardizing the access
    // response shape
    private MeterAccessResponse mapToResponse(MeterAccess access) {
        return MeterAccessResponse.builder()
                .id(access.getId())
                .meterId(access.getMeter().getId())
                .userId(access.getUser().getId())
                .userName(access.getUser().getName())
                .userEmail(access.getUser().getEmail())
                .status(access.getAccessStatus().name())
                .createdAt(access.getCreatedAt())
                .build();
    }
}
