package com.vms.controller;

import com.vms.dto.MeterRequest;
import com.vms.dto.MeterResponse;
import com.vms.security.UserDetailsImpl;
import com.vms.service.MeterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meters")
@RequiredArgsConstructor
public class MeterController {

    private final MeterService meterService;

    @GetMapping
    public ResponseEntity<List<MeterResponse>> getAllMeters(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(meterService.getAllMetersForUser(userDetails.getId()));
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
