package com.vms.service;

import com.vms.dto.BudgetRequest;
import com.vms.dto.BudgetResponse;
import com.vms.entity.Budget;
import com.vms.entity.BudgetAccess;
import com.vms.entity.BudgetTransaction.TransactionType;
import com.vms.entity.User;
import com.vms.repository.BudgetAccessRepository;
import com.vms.repository.BudgetRepository;
import com.vms.repository.BudgetTransactionRepository;
import com.vms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetAccessRepository budgetAccessRepository;
    private final BudgetTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public List<BudgetResponse> getAllForUser(UUID userId) {
        User user = getUser(userId);
        return budgetRepository.findAllAccessibleByUser(user)
                .stream()
                .map(b -> mapToResponse(b, userId))
                .collect(Collectors.toList());
    }

    public BudgetResponse getBudget(UUID budgetId, UUID userId) {
        Budget budget = getBudgetWithAccess(budgetId, userId);
        return mapToResponse(budget, userId);
    }

    @Transactional
    public BudgetResponse create(BudgetRequest request, UUID userId) {
        User owner = getUser(userId);
        Budget budget = Budget.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .owner(owner)
                .build();
        return mapToResponse(budgetRepository.save(budget), userId);
    }

    @Transactional
    public BudgetResponse update(UUID budgetId, BudgetRequest request, UUID userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        if (!budget.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only the budget owner can update it");
        }
        budget.setName(request.getName());
        budget.setDescription(request.getDescription());
        budget.setType(request.getType());
        return mapToResponse(budgetRepository.save(budget), userId);
    }

    @Transactional
    public void delete(UUID budgetId, UUID userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        if (!budget.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only the budget owner can delete it");
        }
        budgetRepository.delete(budget);
    }

    public boolean canUserAccessBudget(UUID userId, UUID budgetId) {
        Budget budget = budgetRepository.findById(budgetId).orElse(null);
        if (budget == null) return false;
        if (budget.getOwner().getId().equals(userId)) return true;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        return budgetAccessRepository.findByBudgetAndUser(budget, user)
                .map(a -> a.getAccessStatus() == BudgetAccess.AccessStatus.APPROVED)
                .orElse(false);
    }

    Budget getBudgetWithAccess(UUID budgetId, UUID userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        if (!canUserAccessBudget(userId, budgetId)) {
            throw new RuntimeException("Access denied to this budget");
        }
        return budget;
    }

    private BudgetResponse mapToResponse(Budget budget, UUID requestingUserId) {
        BigDecimal income = transactionRepository.sumAmountByBudgetAndType(budget, TransactionType.INCOME);
        BigDecimal expenses = transactionRepository.sumAmountByBudgetAndType(budget, TransactionType.EXPENSE);
        if (income == null) income = BigDecimal.ZERO;
        if (expenses == null) expenses = BigDecimal.ZERO;

        int memberCount = budgetAccessRepository.findByBudget(budget).stream()
                .filter(a -> a.getAccessStatus() == BudgetAccess.AccessStatus.APPROVED)
                .mapToInt(a -> 1).sum();

        return BudgetResponse.builder()
                .id(budget.getId())
                .name(budget.getName())
                .description(budget.getDescription())
                .type(budget.getType())
                .ownerId(budget.getOwner().getId())
                .ownerName(budget.getOwner().getName())
                .isOwner(budget.getOwner().getId().equals(requestingUserId))
                .totalIncome(income)
                .totalExpenses(expenses)
                .balance(income.subtract(expenses))
                .memberCount(memberCount)
                .transactionCount((int) transactionRepository.countByBudget(budget))
                .createdAt(budget.getCreatedAt() != null ? budget.getCreatedAt().toString() : null)
                .build();
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
