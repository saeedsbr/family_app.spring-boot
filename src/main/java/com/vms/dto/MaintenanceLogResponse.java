package com.vms.dto;

import com.vms.entity.MaintenanceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceLogResponse {
    private UUID id;
    private UUID vehicleId;
    private String serviceName;
    private String description;
    private MaintenanceType type;
    private Integer odometer;
    private Double cost;
    private LocalDateTime serviceDate;
    private LocalDateTime createdAt;
}
