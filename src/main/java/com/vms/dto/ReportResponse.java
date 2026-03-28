package com.vms.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReportResponse {
    private String period;
    private List<ExpenseReportItem> data;
    private ReportSummary summary;

    @Data
    @Builder
    public static class ExpenseReportItem {
        private LocalDateTime period;
        private Double totalCost;
        private Double totalFuel;
        private Integer fillUps;
        private Double averageFuelEconomy;
    }

    @Data
    @Builder
    public static class ReportSummary {
        private Double totalCost;
        private Double totalFuel;
        private Double averageCostPerKm;
        private Integer totalDistance;
    }
}
