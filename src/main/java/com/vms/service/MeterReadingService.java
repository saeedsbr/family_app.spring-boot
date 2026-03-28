package com.vms.service;

import com.vms.dto.MeterReadingRequest;
import com.vms.dto.MeterReadingResponse;
import com.vms.entity.Meter;
import com.vms.entity.MeterAccess;
import com.vms.entity.MeterReading;
import com.vms.entity.User;
import com.vms.repository.MeterAccessRepository;
import com.vms.repository.MeterReadingRepository;
import com.vms.repository.MeterRepository;
import com.vms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeterReadingService {

    private final MeterReadingRepository readingRepository;
    private final MeterRepository meterRepository;
    private final MeterAccessRepository accessRepository;
    private final UserRepository userRepository;

    public List<MeterReadingResponse> getReadingsForMeter(UUID meterId, UUID userId) {
        Meter meter = validateMeterAccess(meterId, userId);
        return readingRepository.findByMeterOrderByReadingDateDesc(meter).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MeterReadingResponse submitReading(UUID meterId, MeterReadingRequest request, UUID userId) {
        Meter meter = validateMeterAccess(meterId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getReadingValue() == null || request.getReadingValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Reading value must be a positive number");
        }

        // Find the last reading before this date to calculate consumption
        MeterReading previousReading = readingRepository
                .findFirstByMeterAndReadingDateLessThanOrderByReadingDateDesc(meter,
                        request.getReadingDate().plusDays(1))
                .orElse(null);

        // Or simply the absolute latest reading if it's the newest
        // Wait, to be safe, we just get the chronological latest before this date

        if (previousReading != null && request.getReadingValue().compareTo(previousReading.getReadingValue()) < 0) {
            throw new IllegalArgumentException("Reading value cannot be lower than the previous recorded value of "
                    + previousReading.getReadingValue());
        }

        BigDecimal consumption = BigDecimal.ZERO;
        if (previousReading != null) {
            consumption = request.getReadingValue().subtract(previousReading.getReadingValue());
        }

        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .readingDate(request.getReadingDate())
                .readingValue(request.getReadingValue())
                .consumption(consumption)
                .notes(request.getNotes())
                .recordedBy(user)
                .build();

        reading = readingRepository.save(reading);
        return mapToResponse(reading);
    }

    private Meter validateMeterAccess(UUID meterId, UUID userId) {
        Meter meter = meterRepository.findById(meterId)
                .orElseThrow(() -> new RuntimeException("Meter not found"));

        if (meter.getOwner().getId().equals(userId)) {
            return meter; // Owner always has access
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MeterAccess access = accessRepository.findByMeterAndUser(meter, user)
                .orElseThrow(() -> new RuntimeException("You do not have access to this meter"));

        if (access.getAccessStatus() != MeterAccess.AccessStatus.APPROVED) {
            throw new RuntimeException("Access to this meter is pending or rejected");
        }

        return meter;
    }

    private MeterReadingResponse mapToResponse(MeterReading reading) {
        return MeterReadingResponse.builder()
                .id(reading.getId())
                .meterId(reading.getMeter().getId())
                .readingDate(reading.getReadingDate())
                .readingValue(reading.getReadingValue())
                .consumption(reading.getConsumption())
                .notes(reading.getNotes())
                .recordedBy(reading.getRecordedBy().getId())
                .recordedByName(reading.getRecordedBy().getName())
                .build();
    }
}
