package com.lifepulse.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.lifepulse.dto.MeterReadingRequest;
import com.lifepulse.dto.MeterReadingResponse;
import com.lifepulse.service.CurrentUserService;
import com.lifepulse.service.MeterReadingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/meters/{meterId}/readings")
@RequiredArgsConstructor
public class MeterReadingController {

    private final MeterReadingService readingService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<MeterReadingResponse>> getReadings(
            @PathVariable UUID meterId,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(readingService.getReadingsForMeter(meterId, userId));
    }

    @PostMapping
    public ResponseEntity<MeterReadingResponse> addReading(
            @PathVariable UUID meterId,
            @RequestBody MeterReadingRequest request,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(readingService.submitReading(meterId, request, userId));
    }

    @PutMapping("/{readingId}")
    public ResponseEntity<MeterReadingResponse> updateReading(
            @PathVariable UUID meterId,
            @PathVariable UUID readingId,
            @RequestBody MeterReadingRequest request,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(readingService.updateReading(readingId, request, userId));
    }
}
