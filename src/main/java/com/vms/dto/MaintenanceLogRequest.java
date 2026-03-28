package com.vms.dto;

import com.vms.entity.MaintenanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceLogRequest {
    @NotBlank
    private String serviceName;

    private String description;

    @NotNull
    private MaintenanceType type;

    @NotNull
    private Integer odometer;

    @NotNull
    private Double cost;

    @NotNull
    private LocalDateTime serviceDate;
}
