package com.vms.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BudgetStatsResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal balance;
    private int transactionCount;
    private List<MonthStat> monthlyData;
    private List<CategoryStat> categoryBreakdown;
    private List<MemberStat> memberActivity;

    @Data
    @Builder
    public static class MonthStat {
        private String month; // "Jan 2026"
        private BigDecimal income;
        private BigDecimal expenses;
    }

    @Data
    @Builder
    public static class CategoryStat {
        private String category;
        private BigDecimal amount;
        private long count;
    }

    @Data
    @Builder
    public static class MemberStat {
        private String memberName;
        private long transactionCount;
        private BigDecimal totalAmount;
    }
}
