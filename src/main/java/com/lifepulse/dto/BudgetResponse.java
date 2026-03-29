package com.lifepulse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lifepulse.entity.Budget.BudgetType;
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
    @JsonProperty("isOwner")
    private boolean isOwner;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal balance;
    private int memberCount;
    private int transactionCount;
    private String createdAt;
}
