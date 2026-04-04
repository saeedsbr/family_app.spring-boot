package com.lifepulse.controller;

import com.lifepulse.dto.*;
import com.lifepulse.security.UserDetailsImpl;
import com.lifepulse.service.CommitteeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/committees")
@RequiredArgsConstructor
public class CommitteeController {

    private final CommitteeService committeeService;

    @GetMapping
    public ResponseEntity<List<CommitteeResponse>> getAll(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(committeeService.getAllCommitteesForUser(user.getId()));
    }

    @PostMapping("/create")
    public ResponseEntity<CommitteeResponse> create(@Valid @RequestBody CommitteeRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(committeeService.createCommittee(request, user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommitteeResponse> get(@PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(committeeService.getCommitteeById(id, user.getId()));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<CommitteeMemberResponse> addMember(@PathVariable UUID id,
            @Valid @RequestBody CommitteeRequest.MemberRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(committeeService.addMember(id, request, user.getId()));
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<CommitteeTransactionResponse> markPaid(@PathVariable UUID id,
            @Valid @RequestBody CommitteePaymentRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(committeeService.markPaid(id, request, user.getId()));
    }

    @PostMapping("/{id}/distribute-pot")
    public ResponseEntity<CommitteeTransactionResponse> distributePot(@PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(committeeService.distributePot(id, user.getId()));
    }

    @PostMapping("/{id}/cover-default")
    public ResponseEntity<CommitteeTransactionResponse> coverDefault(@PathVariable UUID id,
            @Valid @RequestBody CommitteePaymentRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(committeeService.coverDefault(id, request, user.getId()));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<CommitteeTransactionResponse>> getTransactions(@PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(committeeService.getTransactions(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<CommitteeResponse>> getAvailable(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(committeeService.getAvailableCommittees());
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Void> join(@PathVariable UUID id, @AuthenticationPrincipal UserDetailsImpl user) {
        committeeService.joinCommittee(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/request/{memberId}/approve")
    public ResponseEntity<Void> approve(@PathVariable UUID memberId,
            @RequestBody CommitteeJoinApprovalRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {
        committeeService.approveJoinRequest(memberId, request.getTurn(), user.getId());
        return ResponseEntity.ok().build();
    }
}
