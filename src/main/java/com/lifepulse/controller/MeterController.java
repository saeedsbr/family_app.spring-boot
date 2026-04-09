package com.lifepulse.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.lifepulse.dto.MeterReadingResponse;
import com.lifepulse.dto.MeterRequest;
import com.lifepulse.dto.MeterResponse;
import com.lifepulse.service.CurrentUserService;
import com.lifepulse.service.MeterReadingService;
import com.lifepulse.service.MeterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/meters")
@RequiredArgsConstructor
public class MeterController {

    private final MeterService meterService;
    private final MeterReadingService meterReadingService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<MeterResponse>> getAllMeters(Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(meterService.getAllMetersForUser(userId));
    }

    @GetMapping("/readings/history")
    public ResponseEntity<List<MeterReadingResponse>> getAllReadingsHistory(
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(meterReadingService.getAllReadingsForUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeterResponse> getMeter(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(meterService.getMeter(id, userId));
    }

    @PostMapping
    public ResponseEntity<MeterResponse> addMeter(
            @RequestBody MeterRequest request,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(meterService.create(request, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MeterResponse> updateMeter(
            @PathVariable UUID id,
            @RequestBody MeterRequest request,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(meterService.update(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeter(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        meterService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
