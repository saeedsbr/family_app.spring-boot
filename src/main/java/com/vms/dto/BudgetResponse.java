package com.vms.dto;

import com.vms.entity.Budget.BudgetType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BudgetResponse {
    private UUID id;
    private String name;
    private String description;
    private BudgetType type;
    private UUID ownerId;
    private String ownerName;
    private boolean isOwner;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal balance;
    private int memberCount;
    private int transactionCount;
    private String createdAt;
}
