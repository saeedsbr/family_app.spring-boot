package com.vms.controller;

import com.vms.dto.ExpenseReportDTO;
import com.vms.security.UserDetailsImpl;
import com.vms.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/expenses")
    public ResponseEntity<ExpenseReportDTO> getExpenses(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam UUID vehicleId,
            @RequestParam String period) {
        return ResponseEntity.ok(reportService.getExpenseReport(userDetails.getId(), vehicleId, period));
    }
}
