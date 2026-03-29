package com.lifepulse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseReportDTO {
    private String period;
    private List<DataPoint> data;
    private Summary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private String period;
        private Double totalCost;
        private Double totalFuel;
        private Double maintenanceCost;
        private Integer fillUps;
        private Double averageFuelEconomy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Double totalCost;
        private Double totalFuel;
        private Double maintenanceCost;
        private Double averageCostPerKm;
        private Double totalDistance;
    }
}
