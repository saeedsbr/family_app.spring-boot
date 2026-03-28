package com.vms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FuelLogRequest {
    @NotNull
    private Integer odometer;
    @NotNull
    private Double fuelAmount;
    @NotNull
    private Double totalCost;
    private LocalDateTime logDate;
}
