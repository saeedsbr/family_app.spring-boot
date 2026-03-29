package com.lifepulse.controller;

import com.lifepulse.dto.BudgetStatsResponse;
import com.lifepulse.security.UserDetailsImpl;
import com.lifepulse.service.BudgetTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/budgets/{budgetId}/stats")
@RequiredArgsConstructor
public class BudgetStatsController {

    private final BudgetTransactionService transactionService;

    @GetMapping
    public ResponseEntity<BudgetStatsResponse> getStats(@PathVariable UUID budgetId,
                                                         @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(transactionService.getStats(budgetId, user.getId()));
    }
}
