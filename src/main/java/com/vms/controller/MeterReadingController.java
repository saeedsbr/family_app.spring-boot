package com.vms.controller;

import com.vms.dto.MeterReadingRequest;
import com.vms.dto.MeterReadingResponse;
import com.vms.security.UserDetailsImpl;
import com.vms.service.MeterReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meters/{meterId}/readings")
@RequiredArgsConstructor
public class MeterReadingController {

    private final MeterReadingService readingService;

    @GetMapping
    public ResponseEntity<List<MeterReadingResponse>> getReadings(
            @PathVariable UUID meterId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(readingService.getReadingsForMeter(meterId, userDetails.getId()));
    }

    @PostMapping
    public ResponseEntity<MeterReadingResponse> addReading(
            @PathVariable UUID meterId,
            @RequestBody MeterReadingRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(readingService.submitReading(meterId, request, userDetails.getId()));
    }
}
