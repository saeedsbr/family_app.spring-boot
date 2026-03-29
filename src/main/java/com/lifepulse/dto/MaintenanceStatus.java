package com.lifepulse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaintenanceStatus {
    private Integer kmRemaining;
    private Integer kmSinceLastService;
    private Double progressPercentage;
    private MaintenanceStatusType status;
    private Integer nextServiceAt;
}
