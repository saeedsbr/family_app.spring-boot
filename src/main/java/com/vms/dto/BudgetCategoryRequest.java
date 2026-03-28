package com.vms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BudgetCategoryRequest {
    @NotBlank
    private String name;
    private String color;
}
