package com.lifepulse.controller;

import com.lifepulse.dto.MeterReadingBulkRequest;
import com.lifepulse.dto.MeterReadingResponse;
import com.lifepulse.security.UserDetailsImpl;
import com.lifepulse.service.MeterReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meters/bulk-readings")
@RequiredArgsConstructor
public class BulkMeterReadingController {

    private final MeterReadingService readingService;

    @PostMapping
    public ResponseEntity<List<MeterReadingResponse>> addBulkReadings(
            @RequestBody List<MeterReadingBulkRequest> requests,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(readingService.submitBulkReadings(requests, userDetails.getId()));
    }
}
