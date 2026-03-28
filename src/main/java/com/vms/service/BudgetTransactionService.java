package com.vms.service;

import com.vms.dto.BudgetStatsResponse;
import com.vms.dto.BudgetStatsResponse.CategoryStat;
import com.vms.dto.BudgetStatsResponse.MemberStat;
import com.vms.dto.BudgetStatsResponse.MonthStat;
import com.vms.dto.BudgetTransactionRequest;
import com.vms.dto.BudgetTransactionResponse;
import com.vms.entity.Budget;
import com.vms.entity.BudgetTransaction;
import com.vms.entity.BudgetTransaction.TransactionType;
import com.vms.entity.User;
import com.vms.repository.BudgetTransactionRepository;
import com.vms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetTransactionService {

    private final BudgetTransactionRepository transactionRepository;
    private final BudgetService budgetService;
    private final UserRepository userRepository;

    public List<BudgetTransactionResponse> getTransactions(UUID budgetId, UUID userId) {
        Budget budget = budgetService.getBudgetWithAccess(budgetId, userId);
        return transactionRepository.findByBudgetOrderByDateDesc(budget)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public BudgetTransactionResponse addTransaction(UUID budgetId, BudgetTransactionRequest req, UUID userId) {
        Budget budget = budgetService.getBudgetWithAccess(budgetId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        BudgetTransaction tx = BudgetTransaction.builder()
                .budget(budget)
                .addedBy(user)
                .amount(req.getAmount())
                .type(req.getType())
                .category(req.getCategory())
                .description(req.getDescription())
                .date(req.getDate() != null ? req.getDate() : LocalDate.now())
                .build();
        return mapToResponse(transactionRepository.save(tx));
    }

    @Transactional
    public BudgetTransactionResponse updateTransaction(UUID budgetId, UUID txId, BudgetTransactionRequest req, UUID userId) {
        Budget budget = budgetService.getBudgetWithAccess(budgetId, userId);
        BudgetTransaction tx = transactionRepository.findById(txId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (!tx.getBudget().getId().equals(budget.getId())) {
            throw new RuntimeException("Transaction does not belong to this budget");
        }
        boolean isOwner = budget.getOwner().getId().equals(userId);
        boolean isAuthor = tx.getAddedBy().getId().equals(userId);
        if (!isOwner && !isAuthor) {
            throw new RuntimeException("You can only edit your own transactions");
        }
        tx.setAmount(req.getAmount());
        tx.setType(req.getType());
        tx.setCategory(req.getCategory());
        tx.setDescription(req.getDescription());
        if (req.getDate() != null) tx.setDate(req.getDate());
        return mapToResponse(transactionRepository.save(tx));
    }

    @Transactional
    public void deleteTransaction(UUID budgetId, UUID txId, UUID userId) {
        Budget budget = budgetService.getBudgetWithAccess(budgetId, userId);
        BudgetTransaction tx = transactionRepository.findById(txId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (!tx.getBudget().getId().equals(budget.getId())) {
            throw new RuntimeException("Transaction does not belong to this budget");
        }
        boolean isOwner = budget.getOwner().getId().equals(userId);
        boolean isAuthor = tx.getAddedBy().getId().equals(userId);
        if (!isOwner && !isAuthor) {
            throw new RuntimeException("You can only delete your own transactions");
        }
        transactionRepository.delete(tx);
    }

    public BudgetStatsResponse getStats(UUID budgetId, UUID userId) {
        Budget budget = budgetService.getBudgetWithAccess(budgetId, userId);
        List<BudgetTransaction> all = transactionRepository.findByBudgetOrderByDateDesc(budget);

        BigDecimal totalIncome = transactionRepository.sumAmountByBudgetAndType(budget, TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumAmountByBudgetAndType(budget, TransactionType.EXPENSE);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        // Monthly data — last 6 months
        List<MonthStat> monthlyData = buildMonthlyData(all);

        // Category breakdown (expenses only)
        List<CategoryStat> categoryBreakdown = all.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(BudgetTransaction::getCategory))
                .entrySet().stream()
                .map(e -> CategoryStat.builder()
                        .category(e.getKey())
                        .amount(e.getValue().stream().map(BudgetTransaction::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .count(e.getValue().size())
                        .build())
                .sorted(Comparator.comparing(CategoryStat::getAmount).reversed())
                .collect(Collectors.toList());

        // Member activity
        List<MemberStat> memberActivity = all.stream()
                .collect(Collectors.groupingBy(t -> t.getAddedBy().getName()))
                .entrySet().stream()
                .map(e -> MemberStat.builder()
                        .memberName(e.getKey())
                        .transactionCount(e.getValue().size())
                        .totalAmount(e.getValue().stream().map(BudgetTransaction::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .build())
                .collect(Collectors.toList());

        return BudgetStatsResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .balance(totalIncome.subtract(totalExpenses))
                .transactionCount(all.size())
                .monthlyData(monthlyData)
                .categoryBreakdown(categoryBreakdown)
                .memberActivity(memberActivity)
                .build();
    }

    private List<MonthStat> buildMonthlyData(List<BudgetTransaction> all) {
        List<MonthStat> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            String label = monthStart.format(fmt);

            BigDecimal income = all.stream()
                    .filter(t -> !t.getDate().isBefore(monthStart) && !t.getDate().isAfter(monthEnd))
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .map(BudgetTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expenses = all.stream()
                    .filter(t -> !t.getDate().isBefore(monthStart) && !t.getDate().isAfter(monthEnd))
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(BudgetTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(MonthStat.builder().month(label).income(income).expenses(expenses).build());
        }
        return result;
    }

    private BudgetTransactionResponse mapToResponse(BudgetTransaction tx) {
        return BudgetTransactionResponse.builder()
                .id(tx.getId())
                .budgetId(tx.getBudget().getId())
                .amount(tx.getAmount())
                .type(tx.getType())
                .category(tx.getCategory())
                .description(tx.getDescription())
                .date(tx.getDate() != null ? tx.getDate().toString() : null)
                .addedById(tx.getAddedBy().getId())
                .addedByName(tx.getAddedBy().getName())
                .createdAt(tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : null)
                .build();
    }
}
