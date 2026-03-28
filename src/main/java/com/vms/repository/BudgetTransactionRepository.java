package com.vms.repository;

import com.vms.entity.Budget;
import com.vms.entity.BudgetTransaction;
import com.vms.entity.BudgetTransaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetTransactionRepository extends JpaRepository<BudgetTransaction, UUID> {

    List<BudgetTransaction> findByBudgetOrderByDateDesc(Budget budget);

    List<BudgetTransaction> findByBudgetAndDateBetweenOrderByDateAsc(Budget budget, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM BudgetTransaction t WHERE t.budget = :budget AND t.type = :type")
    BigDecimal sumAmountByBudgetAndType(@Param("budget") Budget budget, @Param("type") TransactionType type);

    long countByBudget(Budget budget);
}
