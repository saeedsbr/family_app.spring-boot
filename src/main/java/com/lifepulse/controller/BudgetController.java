package com.lifepulse.controller;

import com.lifepulse.dto.BudgetAccessResponse;
import com.lifepulse.dto.BudgetRequest;
import com.lifepulse.dto.BudgetResponse;
import com.lifepulse.service.BudgetAccessService;
import com.lifepulse.service.BudgetService;
import com.lifepulse.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetAccessService budgetAccessService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAll(Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(budgetService.getAllForUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> get(@PathVariable UUID id,
                                               Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(budgetService.getBudget(id, userId));
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> create(@Valid @RequestBody BudgetRequest request,
                                                  Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(budgetService.create(request, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody BudgetRequest request,
                                                  Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(budgetService.update(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        budgetService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // --- Member management ---

    @GetMapping("/{id}/members")
    public ResponseEntity<List<BudgetAccessResponse>> getMembers(@PathVariable UUID id,
                                                                   Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(budgetAccessService.getMembersForBudget(id, userId));
    }

    @PostMapping("/{id}/members/invite")
    public ResponseEntity<BudgetAccessResponse> invite(@PathVariable UUID id,
                                                        @RequestBody Map<String, String> body,
                                                        Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(budgetAccessService.inviteByEmail(id, body.get("email"), userId));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID id,
                                              @PathVariable UUID memberId,
                                              Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        budgetAccessService.removeMember(id, memberId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pending-requests")
    public ResponseEntity<List<BudgetAccessResponse>> getPending(@PathVariable UUID id,
                                                                   Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(budgetAccessService.getPendingRequestsForOwner(userId));
    }

    @PutMapping("/{id}/members/{requestId}/approve")
    public ResponseEntity<BudgetAccessResponse> approve(@PathVariable UUID id,
                                                         @PathVariable UUID requestId,
                                                         Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(budgetAccessService.approveRequest(userId, requestId));
    }

    @PutMapping("/{id}/members/{requestId}/reject")
    public ResponseEntity<BudgetAccessResponse> reject(@PathVariable UUID id,
                                                        @PathVariable UUID requestId,
                                                        Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(budgetAccessService.rejectRequest(userId, requestId));
    }
}
