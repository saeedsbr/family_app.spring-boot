package com.lifepulse.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.lifepulse.dto.MeterReadingResponse;
import com.lifepulse.dto.MeterRequest;
import com.lifepulse.dto.MeterResponse;
import com.lifepulse.security.UserDetailsImpl;
import com.lifepulse.service.MeterReadingService;
import com.lifepulse.service.MeterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/meters")
@RequiredArgsConstructor
public class MeterController {

    private final MeterService meterService;
    private final MeterReadingService meterReadingService;

    @GetMapping
    public ResponseEntity<List<MeterResponse>> getAllMeters(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(meterService.getAllMetersForUser(userDetails.getId()));
    }

    @GetMapping("/readings/history")
    public ResponseEntity<List<MeterReadingResponse>> getAllReadingsHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(meterReadingService.getAllReadingsForUser(userDetails.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeterResponse> getMeter(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(meterService.getMeter(id, userDetails.getId()));
    }

    @PostMapping
    public ResponseEntity<MeterResponse> addMeter(
            @RequestBody MeterRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(meterService.create(request, userDetails.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MeterResponse> updateMeter(
            @PathVariable UUID id,
            @RequestBody MeterRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(meterService.update(id, request, userDetails.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeter(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        meterService.delete(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
