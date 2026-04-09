package com.lifepulse.controller;

import com.lifepulse.dto.BudgetStatsResponse;
import com.lifepulse.service.BudgetTransactionService;
import com.lifepulse.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/budgets/{budgetId}/stats")
@RequiredArgsConstructor
public class BudgetStatsController {

    private final BudgetTransactionService transactionService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<BudgetStatsResponse> getStats(@PathVariable UUID budgetId,
                                                         Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(transactionService.getStats(budgetId, userId));
    }
}
