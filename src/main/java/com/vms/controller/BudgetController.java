package com.vms.controller;

import com.vms.dto.BudgetAccessResponse;
import com.vms.dto.BudgetRequest;
import com.vms.dto.BudgetResponse;
import com.vms.security.UserDetailsImpl;
import com.vms.service.BudgetAccessService;
import com.vms.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAll(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(budgetService.getAllForUser(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> get(@PathVariable UUID id,
                                               @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(budgetService.getBudget(id, user.getId()));
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> create(@Valid @RequestBody BudgetRequest request,
                                                  @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(budgetService.create(request, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody BudgetRequest request,
                                                  @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(budgetService.update(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserDetailsImpl user) {
        budgetService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    // --- Member management ---

    @GetMapping("/{id}/members")
    public ResponseEntity<List<BudgetAccessResponse>> getMembers(@PathVariable UUID id,
                                                                   @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(budgetAccessService.getMembersForBudget(id, user.getId()));
    }

    @PostMapping("/{id}/members/invite")
    public ResponseEntity<BudgetAccessResponse> invite(@PathVariable UUID id,
                                                        @RequestBody Map<String, String> body,
                                                        @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(budgetAccessService.inviteByEmail(id, body.get("email"), user.getId()));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID id,
                                              @PathVariable UUID memberId,
                                              @AuthenticationPrincipal UserDetailsImpl user) {
        budgetAccessService.removeMember(id, memberId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pending-requests")
    public ResponseEntity<List<BudgetAccessResponse>> getPending(@PathVariable UUID id,
                                                                   @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(budgetAccessService.getPendingRequestsForOwner(user.getId()));
    }

    @PutMapping("/{id}/members/{requestId}/approve")
    public ResponseEntity<BudgetAccessResponse> approve(@PathVariable UUID id,
                                                         @PathVariable UUID requestId,
                                                         @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(budgetAccessService.approveRequest(user.getId(), requestId));
    }

    @PutMapping("/{id}/members/{requestId}/reject")
    public ResponseEntity<BudgetAccessResponse> reject(@PathVariable UUID id,
                                                        @PathVariable UUID requestId,
                                                        @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(budgetAccessService.rejectRequest(user.getId(), requestId));
    }
}
