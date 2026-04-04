package com.lifepulse.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifepulse.dto.MeterReadingRequest;
import com.lifepulse.dto.MeterReadingResponse;
import com.lifepulse.entity.Meter;
import com.lifepulse.entity.MeterAccess;
import com.lifepulse.entity.MeterReading;
import com.lifepulse.entity.User;
import com.lifepulse.repository.MeterAccessRepository;
import com.lifepulse.repository.MeterReadingRepository;
import com.lifepulse.repository.MeterRepository;
import com.lifepulse.repository.UserRepository;

import lombok.RequiredArgsConstructor;

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

        public List<MeterReadingResponse> getAllReadingsForUser(UUID userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Get owned meters
                List<Meter> ownedMeters = meterRepository.findByOwner(user);

                // Get accessible meters
                List<Meter> accessibleMeters = accessRepository
                                .findByUserAndAccessStatus(user, MeterAccess.AccessStatus.APPROVED)
                                .stream()
                                .map(MeterAccess::getMeter)
                                .collect(Collectors.toList());

                // Combine them
                List<Meter> allMeters = Stream.concat(ownedMeters.stream(), accessibleMeters.stream())
                                .distinct()
                                .collect(Collectors.toList());

                if (allMeters.isEmpty()) {
                        return new ArrayList<>();
                }

                return readingRepository.findByMeterInOrderByReadingDateDesc(allMeters).stream()
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

                if (previousReading != null
                                && request.getReadingValue().compareTo(previousReading.getReadingValue()) < 0) {
                        throw new IllegalArgumentException(
                                        "Reading value cannot be lower than the previous recorded value of "
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
                                .recordedByManual(request.getRecordedByManual())
                                .build();

                reading = readingRepository.save(reading);
                return mapToResponse(reading);
        }

        @Transactional
        public MeterReadingResponse updateReading(UUID readingId, MeterReadingRequest request, UUID userId) {
                MeterReading reading = readingRepository.findById(readingId)
                                .orElseThrow(() -> new RuntimeException("Reading not found"));

                validateMeterAccess(reading.getMeter().getId(), userId);

                // Update basic fields
                reading.setReadingValue(request.getReadingValue());
                reading.setReadingDate(request.getReadingDate());
                reading.setNotes(request.getNotes());
                if (request.getRecordedByManual() != null) {
                        reading.setRecordedByManual(request.getRecordedByManual());
                }

                // 1. Recalculate this reading's consumption
                MeterReading previousReading = readingRepository
                                .findFirstByMeterAndReadingDateLessThanOrderByReadingDateDesc(reading.getMeter(),
                                                reading.getReadingDate())
                                .orElse(null);

                if (previousReading != null) {
                        reading.setConsumption(reading.getReadingValue().subtract(previousReading.getReadingValue()));
                } else {
                        reading.setConsumption(BigDecimal.ZERO);
                }

                // 2. Recalculate next reading's consumption (if any)
                MeterReading nextReading = readingRepository
                                .findFirstByMeterAndReadingDateGreaterThanOrderByReadingDateAsc(reading.getMeter(),
                                                reading.getReadingDate())
                                .orElse(null);

                if (nextReading != null) {
                        nextReading.setConsumption(nextReading.getReadingValue().subtract(reading.getReadingValue()));
                        readingRepository.save(nextReading);
                }

                reading = readingRepository.save(reading);
                return mapToResponse(reading);
        }

        @Transactional
        public List<MeterReadingResponse> submitBulkReadings(List<com.lifepulse.dto.MeterReadingBulkRequest> requests,
                        UUID userId) {
                return requests.stream()
                                .map(req -> {
                                        MeterReadingRequest r = new MeterReadingRequest();
                                        r.setReadingValue(java.math.BigDecimal.valueOf(req.getReadingValue()));
                                        r.setReadingDate(java.time.LocalDate
                                                        .parse(req.getReadingDate().substring(0, 10)));
                                        r.setRecordedByManual(req.getRecordedByManual());
                                        r.setNotes(req.getNotes());
                                        return submitReading(req.getMeterId(), r, userId);
                                })
                                .collect(Collectors.toList());
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
                                .recordedByManual(reading.getRecordedByManual())
                                .recordedBy(reading.getRecordedBy().getId())
                                .recordedByName(reading.getRecordedByManual() != null ? reading.getRecordedByManual()
                                                : reading.getRecordedBy().getName())
                                .build();
        }
}
