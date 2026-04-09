package com.lifepulse.controller;

import com.lifepulse.dto.*;
import com.lifepulse.service.CommitteeService;
import com.lifepulse.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/committees")
@RequiredArgsConstructor
public class CommitteeController {

    private final CommitteeService committeeService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<CommitteeResponse>> getAll(Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(committeeService.getAllCommitteesForUser(userId));
    }

    @PostMapping("/create")
    public ResponseEntity<CommitteeResponse> create(@Valid @RequestBody CommitteeRequest request,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(committeeService.createCommittee(request, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommitteeResponse> get(@PathVariable UUID id,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(committeeService.getCommitteeById(id, userId));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<CommitteeMemberResponse> addMember(@PathVariable UUID id,
            @Valid @RequestBody CommitteeRequest.MemberRequest request,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(committeeService.addMember(id, request, userId));
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<CommitteeTransactionResponse> markPaid(@PathVariable UUID id,
            @Valid @RequestBody CommitteePaymentRequest request,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(committeeService.markPaid(id, request, userId));
    }

    @PostMapping("/{id}/distribute-pot")
    public ResponseEntity<CommitteeTransactionResponse> distributePot(@PathVariable UUID id,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(committeeService.distributePot(id, userId));
    }

    @PostMapping("/{id}/cover-default")
    public ResponseEntity<CommitteeTransactionResponse> coverDefault(@PathVariable UUID id,
            @Valid @RequestBody CommitteePaymentRequest request,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(committeeService.coverDefault(id, request, userId));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<CommitteeTransactionResponse>> getTransactions(@PathVariable UUID id,
            Authentication authentication) {
        return ResponseEntity.ok(committeeService.getTransactions(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<CommitteeResponse>> getAvailable(Authentication authentication) {
        return ResponseEntity.ok(committeeService.getAvailableCommittees());
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Void> join(@PathVariable UUID id, Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        committeeService.joinCommittee(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/request/{memberId}/approve")
    public ResponseEntity<Void> approve(@PathVariable UUID memberId,
            @RequestBody CommitteeJoinApprovalRequest request,
            Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        committeeService.approveJoinRequest(memberId, request.getTurnCycle(), userId);
        return ResponseEntity.ok().build();
    }
}
