package com.lifepulse.dto;

import com.lifepulse.entity.BudgetTransaction.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BudgetTransactionResponse {
    private UUID id;
    private UUID budgetId;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private String description;
    private String date;
    private UUID addedById;
    private String addedByName;
    private String createdAt;
}
