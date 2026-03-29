package com.lifepulse.repository;

import com.lifepulse.entity.BudgetCategory;
import com.lifepulse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, UUID> {

    @Query("SELECT c FROM BudgetCategory c WHERE c.isSystem = true OR c.owner = :owner")
    List<BudgetCategory> findSystemAndUserCategories(@Param("owner") User owner);
}
