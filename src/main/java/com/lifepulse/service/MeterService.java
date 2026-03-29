package com.lifepulse.service;

import com.lifepulse.dto.MeterRequest;
import com.lifepulse.dto.MeterResponse;
import com.lifepulse.entity.Meter;
import com.lifepulse.entity.User;
import com.lifepulse.repository.MeterRepository;
import com.lifepulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeterService {

    private final MeterRepository meterRepository;
    private final UserRepository userRepository;

    public List<MeterResponse> getAllMetersForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return meterRepository.findAllMetersAccessibleByUser(user).stream()
                .map(m -> mapToResponse(m, userId))
                .distinct()
                .collect(Collectors.toList());
    }

    public MeterResponse getMeter(UUID meterId, UUID userId) {
        Meter meter = meterRepository.findById(meterId)
                .orElseThrow(() -> new RuntimeException("Meter not found"));
        return mapToResponse(meter, userId);
    }

    @Transactional
    public MeterResponse create(MeterRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (meterRepository.existsByIdentifier(request.getIdentifier())) {
            throw new RuntimeException("Meter identifier already exists");
        }

        Meter meter = Meter.builder()
                .name(request.getName())
                .identifier(request.getIdentifier())
                .description(request.getDescription())
                .owner(user)
                .build();

        meter = meterRepository.save(meter);
        return mapToResponse(meter, userId);
    }

    @Transactional
    public MeterResponse update(UUID meterId, MeterRequest request, UUID userId) {
        Meter meter = meterRepository.findById(meterId)
                .orElseThrow(() -> new RuntimeException("Meter not found"));

        if (!meter.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only the meter owner can update it");
        }

        meter.setName(request.getName());
        meter.setDescription(request.getDescription());
        if (request.getIdentifier() != null && !request.getIdentifier().equals(meter.getIdentifier())) {
            if (meterRepository.existsByIdentifier(request.getIdentifier())) {
                throw new RuntimeException("Meter identifier already exists");
            }
            meter.setIdentifier(request.getIdentifier());
        }

        meter = meterRepository.save(meter);
        return mapToResponse(meter, userId);
    }

    @Transactional
    public void delete(UUID meterId, UUID userId) {
        Meter meter = meterRepository.findById(meterId)
                .orElseThrow(() -> new RuntimeException("Meter not found"));

        if (!meter.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only the meter owner can delete it");
        }

        meterRepository.delete(meter);
    }

    private MeterResponse mapToResponse(Meter meter, UUID currentUserId) {
        return MeterResponse.builder()
                .id(meter.getId())
                .name(meter.getName())
                .identifier(meter.getIdentifier())
                .description(meter.getDescription())
                .ownerId(meter.getOwner().getId())
                .isOwner(meter.getOwner().getId().equals(currentUserId))
                .build();
    }
}
