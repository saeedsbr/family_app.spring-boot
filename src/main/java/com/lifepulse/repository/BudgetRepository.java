package com.lifepulse.repository;

import com.lifepulse.entity.Budget;
import com.lifepulse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByOwner(User owner);

    @Query("SELECT DISTINCT b FROM Budget b LEFT JOIN b.members a " +
           "WHERE b.owner = :user OR (a.user = :user AND a.accessStatus = 'APPROVED')")
    List<Budget> findAllAccessibleByUser(@Param("user") User user);
}
