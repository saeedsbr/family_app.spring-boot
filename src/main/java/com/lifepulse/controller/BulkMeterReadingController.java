package com.lifepulse.controller;

import com.lifepulse.dto.MeterReadingBulkRequest;
import com.lifepulse.dto.MeterReadingResponse;
import com.lifepulse.service.CurrentUserService;
import com.lifepulse.service.MeterReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meters/bulk-readings")
@RequiredArgsConstructor
public class BulkMeterReadingController {

    private final MeterReadingService readingService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<List<MeterReadingResponse>> addBulkReadings(
            @RequestBody List<MeterReadingBulkRequest> requests,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(readingService.submitBulkReadings(requests, userId));
    }
}
