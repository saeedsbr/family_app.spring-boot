package com.vms.service;

import com.vms.dto.BudgetAccessResponse;
import com.vms.entity.Budget;
import com.vms.entity.BudgetAccess;
import com.vms.entity.BudgetAccess.AccessStatus;
import com.vms.entity.User;
import com.vms.repository.BudgetAccessRepository;
import com.vms.repository.BudgetRepository;
import com.vms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetAccessService {

    private final BudgetAccessRepository accessRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    @Transactional
    public BudgetAccessResponse inviteByEmail(UUID budgetId, String email, UUID ownerId) {
        Budget budget = getBudget(budgetId);
        if (!budget.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Only the budget owner can invite members");
        }
        User invitee = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("No user found with email: " + email));
        if (invitee.getId().equals(ownerId)) {
            throw new RuntimeException("You cannot invite yourself");
        }
        if (accessRepository.existsByBudgetAndUser(budget, invitee)) {
            throw new RuntimeException("User already has access or a pending request");
        }
        BudgetAccess access = BudgetAccess.builder()
                .budget(budget)
                .user(invitee)
                .accessStatus(AccessStatus.APPROVED)
                .build();
        return mapToResponse(accessRepository.save(access));
    }

    @Transactional
    public BudgetAccessResponse approveRequest(UUID ownerId, UUID requestId) {
        BudgetAccess access = getAccess(requestId);
        assertOwner(access.getBudget(), ownerId, "approve");
        access.setAccessStatus(AccessStatus.APPROVED);
        return mapToResponse(accessRepository.save(access));
    }

    @Transactional
    public BudgetAccessResponse rejectRequest(UUID ownerId, UUID requestId) {
        BudgetAccess access = getAccess(requestId);
        assertOwner(access.getBudget(), ownerId, "reject");
        access.setAccessStatus(AccessStatus.REJECTED);
        return mapToResponse(accessRepository.save(access));
    }

    @Transactional
    public void removeMember(UUID budgetId, UUID memberId, UUID ownerId) {
        Budget budget = getBudget(budgetId);
        assertOwner(budget, ownerId, "remove members from");
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        BudgetAccess access = accessRepository.findByBudgetAndUser(budget, member)
                .orElseThrow(() -> new RuntimeException("Member not found in this budget"));
        accessRepository.delete(access);
    }

    public List<BudgetAccessResponse> getMembersForBudget(UUID budgetId, UUID requestingUserId) {
        Budget budget = getBudget(budgetId);
        return accessRepository.findByBudget(budget)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BudgetAccessResponse> getPendingRequestsForOwner(UUID ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return accessRepository.findByBudgetOwnerAndAccessStatus(owner, AccessStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Budget getBudget(UUID budgetId) {
        return budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
    }

    private BudgetAccess getAccess(UUID requestId) {
        return accessRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Access request not found"));
    }

    private void assertOwner(Budget budget, UUID userId, String action) {
        if (!budget.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only the budget owner can " + action + " this budget");
        }
    }

    private BudgetAccessResponse mapToResponse(BudgetAccess access) {
        return BudgetAccessResponse.builder()
                .id(access.getId())
                .budgetId(access.getBudget().getId())
                .budgetName(access.getBudget().getName())
                .userId(access.getUser().getId())
                .userName(access.getUser().getName())
                .userEmail(access.getUser().getEmail())
                .status(access.getAccessStatus())
                .createdAt(access.getCreatedAt() != null ? access.getCreatedAt().toString() : null)
                .build();
    }
}
