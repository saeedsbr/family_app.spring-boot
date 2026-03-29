package com.lifepulse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleExpenseSummaryResponse {
    private double totalFuelCost;
    private double totalMaintenanceCost;
    private double totalVehicleCost;
    private int ownedVehicleCount;
}
