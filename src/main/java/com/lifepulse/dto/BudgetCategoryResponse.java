package com.lifepulse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BudgetCategoryResponse {
    private UUID id;
    private String name;
    private String color;
    @JsonProperty("isSystem")
    private boolean isSystem;
}
