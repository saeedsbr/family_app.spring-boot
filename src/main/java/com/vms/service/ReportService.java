package com.vms.service;

import com.vms.dto.ExpenseReportDTO;
import com.vms.entity.FuelLog;
import com.vms.entity.Vehicle;
import com.vms.entity.VehicleAccess;
import com.vms.repository.FuelLogRepository;
import com.vms.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

        private final FuelLogRepository fuelLogRepository;
        private final VehicleAccessService vehicleAccessService;

        /**
         * Generate expense report. Period can be "day", "week", "month", or "year".
         * Automatically calculates date range based on the period.
         */
        public ExpenseReportDTO getExpenseReport(UUID userId, UUID vehicleId, String period) {
                if (!vehicleAccessService.canUserAccessVehicle(userId, vehicleId)) {
                        throw new RuntimeException("You do not have access to this vehicle's reports");
                }

                LocalDateTime endDate = LocalDateTime.now();
                LocalDateTime startDate = calculateStartDate(endDate, period);

                // Get all fuel logs in range
                List<FuelLog> logs = fuelLogRepository.findByVehicleIdAndLogDateBetweenOrderByLogDateAsc(
                                vehicleId, startDate, endDate);

                // Group by period
                Map<String, List<FuelLog>> grouped = groupByPeriod(logs, period);

                // Build data points
                List<ExpenseReportDTO.DataPoint> dataPoints = grouped.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .map(entry -> {
                                        List<FuelLog> periodLogs = entry.getValue();
                                        double totalCost = periodLogs.stream()
                                                        .mapToDouble(l -> l.getTotalCost() != null ? l.getTotalCost()
                                                                        : 0)
                                                        .sum();
                                        double totalFuel = periodLogs.stream()
                                                        .mapToDouble(l -> l.getFuelAmount() != null ? l.getFuelAmount()
                                                                        : 0)
                                                        .sum();
                                        return ExpenseReportDTO.DataPoint.builder()
                                                        .period(entry.getKey())
                                                        .totalCost(totalCost)
                                                        .totalFuel(totalFuel)
                                                        .fillUps(periodLogs.size())
                                                        .averageFuelEconomy(totalFuel > 0 ? totalCost / totalFuel : 0.0)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // Build summary
                double totalCost = dataPoints.stream().mapToDouble(ExpenseReportDTO.DataPoint::getTotalCost).sum();
                double totalFuel = dataPoints.stream().mapToDouble(ExpenseReportDTO.DataPoint::getTotalFuel).sum();

                // Calculate total distance from odometer readings
                double totalDistance = 0;
                if (!logs.isEmpty()) {
                        int minOdometer = logs.stream()
                                        .filter(l -> l.getOdometer() != null)
                                        .mapToInt(FuelLog::getOdometer)
                                        .min().orElse(0);
                        int maxOdometer = logs.stream()
                                        .filter(l -> l.getOdometer() != null)
                                        .mapToInt(FuelLog::getOdometer)
                                        .max().orElse(0);
                        totalDistance = maxOdometer - minOdometer;
                }

                double avgCostPerKm = totalDistance > 0 ? totalCost / totalDistance : 0;

                return ExpenseReportDTO.builder()
                                .period(period)
                                .data(dataPoints)
                                .summary(ExpenseReportDTO.Summary.builder()
                                                .totalCost(totalCost)
                                                .totalFuel(totalFuel)
                                                .averageCostPerKm(avgCostPerKm)
                                                .totalDistance(totalDistance)
                                                .build())
                                .build();
        }

        private LocalDateTime calculateStartDate(LocalDateTime endDate, String period) {
                return switch (period.toLowerCase()) {
                        case "day" -> endDate.minusDays(30); // Last 30 days, grouped by day
                        case "week" -> endDate.minusWeeks(12); // Last 12 weeks
                        case "month" -> endDate.minusMonths(12); // Last 12 months
                        case "year" -> endDate.minusYears(5); // Last 5 years
                        default -> endDate.minusMonths(12);
                };
        }

        private Map<String, List<FuelLog>> groupByPeriod(List<FuelLog> logs, String period) {
                DateTimeFormatter formatter = switch (period.toLowerCase()) {
                        case "day" -> DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        case "week" -> DateTimeFormatter.ofPattern("yyyy-'W'ww");
                        case "month" -> DateTimeFormatter.ofPattern("yyyy-MM");
                        case "year" -> DateTimeFormatter.ofPattern("yyyy");
                        default -> DateTimeFormatter.ofPattern("yyyy-MM");
                };

                return logs.stream()
                                .filter(log -> log.getLogDate() != null)
                                .collect(Collectors.groupingBy(
                                                log -> log.getLogDate().format(formatter),
                                                LinkedHashMap::new,
                                                Collectors.toList()));
        }
}
