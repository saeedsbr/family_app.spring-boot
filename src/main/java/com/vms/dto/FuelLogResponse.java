package com.vms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuelLogResponse {
    private UUID id;
    private UUID vehicleId;
    private Integer odometer;
    private Double fuelAmount;
    private Double totalCost;
    private String logDate;
    private Double fuelEconomy;
    private String createdAt;
}
