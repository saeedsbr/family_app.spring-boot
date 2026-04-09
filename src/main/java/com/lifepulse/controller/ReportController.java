package com.lifepulse.controller;

import com.lifepulse.dto.ExpenseReportDTO;
import com.lifepulse.service.CurrentUserService;
import com.lifepulse.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final CurrentUserService currentUserService;

    @GetMapping("/expenses")
    public ResponseEntity<ExpenseReportDTO> getExpenses(
            Authentication authentication,
            @RequestParam UUID vehicleId,
            @RequestParam String period) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(reportService.getExpenseReport(userId, vehicleId, period));
    }
}
