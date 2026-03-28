package com.vms.dto;

import com.vms.entity.BudgetAccess.AccessStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BudgetAccessResponse {
    private UUID id;
    private UUID budgetId;
    private String budgetName;
    private UUID userId;
    private String userName;
    private String userEmail;
    private AccessStatus status;
    private String createdAt;
}
