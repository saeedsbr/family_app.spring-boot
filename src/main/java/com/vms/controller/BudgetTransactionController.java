package com.vms.controller;

import com.vms.dto.BudgetTransactionRequest;
import com.vms.dto.BudgetTransactionResponse;
import com.vms.security.UserDetailsImpl;
import com.vms.service.BudgetTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets/{budgetId}/transactions")
@RequiredArgsConstructor
public class BudgetTransactionController {

    private final BudgetTransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<BudgetTransactionResponse>> getAll(@PathVariable UUID budgetId,
                                                                    @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(transactionService.getTransactions(budgetId, user.getId()));
    }

    @PostMapping
    public ResponseEntity<BudgetTransactionResponse> add(@PathVariable UUID budgetId,
                                                          @Valid @RequestBody BudgetTransactionRequest request,
                                                          @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(transactionService.addTransaction(budgetId, request, user.getId()));
    }

    @PutMapping("/{txId}")
    public ResponseEntity<BudgetTransactionResponse> update(@PathVariable UUID budgetId,
                                                             @PathVariable UUID txId,
                                                             @Valid @RequestBody BudgetTransactionRequest request,
                                                             @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(transactionService.updateTransaction(budgetId, txId, request, user.getId()));
    }

    @DeleteMapping("/{txId}")
    public ResponseEntity<Void> delete(@PathVariable UUID budgetId,
                                        @PathVariable UUID txId,
                                        @AuthenticationPrincipal UserDetailsImpl user) {
        transactionService.deleteTransaction(budgetId, txId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
