package com.vms.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BudgetCategoryResponse {
    private UUID id;
    private String name;
    private String color;
    private boolean isSystem;
}
