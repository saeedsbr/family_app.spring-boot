package com.vms.dto;

import com.vms.entity.Budget.BudgetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BudgetRequest {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private BudgetType type;
}
