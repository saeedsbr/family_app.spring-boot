package com.lifepulse.controller;

import com.lifepulse.dto.BudgetTransactionRequest;
import com.lifepulse.dto.BudgetTransactionResponse;
import com.lifepulse.service.BudgetTransactionService;
import com.lifepulse.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets/{budgetId}/transactions")
@RequiredArgsConstructor
public class BudgetTransactionController {

    private final BudgetTransactionService transactionService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<BudgetTransactionResponse>> getAll(@PathVariable UUID budgetId,
                                                                    Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(transactionService.getTransactions(budgetId, userId));
    }

    @PostMapping
    public ResponseEntity<BudgetTransactionResponse> add(@PathVariable UUID budgetId,
                                                          @Valid @RequestBody BudgetTransactionRequest request,
                                                          Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(transactionService.addTransaction(budgetId, request, userId));
    }

    @PutMapping("/{txId}")
    public ResponseEntity<BudgetTransactionResponse> update(@PathVariable UUID budgetId,
                                                             @PathVariable UUID txId,
                                                             @Valid @RequestBody BudgetTransactionRequest request,
                                                             Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(transactionService.updateTransaction(budgetId, txId, request, userId));
    }

    @DeleteMapping("/{txId}")
    public ResponseEntity<Void> delete(@PathVariable UUID budgetId,
                                        @PathVariable UUID txId,
                                        Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        transactionService.deleteTransaction(budgetId, txId, userId);
        return ResponseEntity.noContent().build();
    }
}
