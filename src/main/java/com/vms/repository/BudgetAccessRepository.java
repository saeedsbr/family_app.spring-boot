package com.vms.repository;

import com.vms.entity.Budget;
import com.vms.entity.BudgetAccess;
import com.vms.entity.BudgetAccess.AccessStatus;
import com.vms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetAccessRepository extends JpaRepository<BudgetAccess, UUID> {

    Optional<BudgetAccess> findByBudgetAndUser(Budget budget, User user);

    List<BudgetAccess> findByBudget(Budget budget);

    List<BudgetAccess> findByUser(User user);

    boolean existsByBudgetAndUser(Budget budget, User user);

    List<BudgetAccess> findByBudgetOwnerAndAccessStatus(User owner, AccessStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM BudgetAccess a WHERE a.budget = :budget")
    void deleteByBudget(@Param("budget") Budget budget);
}
