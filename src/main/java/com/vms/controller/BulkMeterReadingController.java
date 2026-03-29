package com.vms.controller;

import com.vms.dto.MeterReadingBulkRequest;
import com.vms.dto.MeterReadingResponse;
import com.vms.security.UserDetailsImpl;
import com.vms.service.MeterReadingService;
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
