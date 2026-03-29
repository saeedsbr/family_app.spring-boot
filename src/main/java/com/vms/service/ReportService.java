package com.vms.service;

import com.vms.dto.ExpenseReportDTO;
import com.vms.entity.FuelLog;
import com.vms.entity.MaintenanceLog;
import com.vms.entity.Vehicle;
import com.vms.entity.VehicleAccess;
import com.vms.repository.FuelLogRepository;
import com.vms.repository.MaintenanceLogRepository;
import com.vms.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

        private final FuelLogRepository fuelLogRepository;
        private final MaintenanceLogRepository maintenanceLogRepository;
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
                List<FuelLog> logs = fuelLogRepository.findByVehicleIdAndLogDateBetweenOrderByLogDateAsc(vehicleId,
                                startDate, endDate);

                // Switch to a more stable retrieval for maintenance logs
                List<MaintenanceLog> mLogs = maintenanceLogRepository
                                .findByVehicleIdOrderByServiceDateDesc(vehicleId)
                                .stream()
                                .filter(m -> !m.getServiceDate().isBefore(startDate)
                                                && !m.getServiceDate().isAfter(endDate))
                                .sorted(Comparator.comparing(MaintenanceLog::getServiceDate))
                                .collect(Collectors.toList());

                // Group by period
                Map<String, List<FuelLog>> groupedLogs = logs.stream()
                                .collect(Collectors.groupingBy(l -> formatPeriod(l.getLogDate(), period)));

                Map<String, List<MaintenanceLog>> groupedMLogs = mLogs.stream()
                                .collect(Collectors.groupingBy(l -> formatPeriod(l.getServiceDate(), period)));

                // Get all unique period keys in order
                List<String> periodKeys = new ArrayList<>();
                LocalDateTime tempDate = startDate;
                while (tempDate.isBefore(endDate) || tempDate.isEqual(endDate)) {
                        periodKeys.add(formatPeriod(tempDate, period));
                        tempDate = incrementDate(tempDate, period);
                }
                periodKeys = periodKeys.stream().distinct().collect(Collectors.toList());

                List<ExpenseReportDTO.DataPoint> dataPoints = new ArrayList<>();
                for (String key : periodKeys) {
                        List<FuelLog> periodLogs = groupedLogs.getOrDefault(key, Collections.emptyList());
                        List<MaintenanceLog> periodMLogs = groupedMLogs.getOrDefault(key, Collections.emptyList());

                        double periodFuelCost = periodLogs.stream()
                                        .mapToDouble(l -> l.getTotalCost() != null ? l.getTotalCost() : 0).sum();
                        double periodMaintenanceCost = periodMLogs.stream()
                                        .mapToDouble(l -> l.getCost() != null ? l.getCost() : 0).sum();
                        double periodFuelAmount = periodLogs.stream()
                                        .mapToDouble(l -> l.getFuelAmount() != null ? l.getFuelAmount() : 0).sum();

                        dataPoints.add(ExpenseReportDTO.DataPoint.builder()
                                        .period(key)
                                        .totalCost(periodFuelCost + periodMaintenanceCost)
                                        .totalFuel(periodFuelAmount)
                                        .maintenanceCost(periodMaintenanceCost)
                                        .fillUps(periodLogs.size())
                                        .averageFuelEconomy(periodFuelAmount > 0 ? periodLogs.stream()
                                                        .filter(l -> l.getFuelEconomy() != null)
                                                        .mapToDouble(FuelLog::getFuelEconomy)
                                                        .average().orElse(0.0) : 0.0)
                                        .build());
                }

                // Calculate summary
                double totalFuelCost = logs.stream().mapToDouble(l -> l.getTotalCost() != null ? l.getTotalCost() : 0)
                                .sum();
                double totalMaintenanceCost = mLogs.stream().mapToDouble(l -> l.getCost() != null ? l.getCost() : 0)
                                .sum();
                double totalFuel = logs.stream().mapToDouble(l -> l.getFuelAmount() != null ? l.getFuelAmount() : 0)
                                .sum();
                double totalCost = totalFuelCost + totalMaintenanceCost;

                // Calculate total distance from odometer readings
                double totalDistance = 0;
                if (!logs.isEmpty()) {
                        Vehicle vehicle = logs.get(0).getVehicle();
                        int minOdometerInLogs = logs.stream()
                                        .filter(l -> l.getOdometer() != null)
                                        .mapToInt(FuelLog::getOdometer)
                                        .min().orElse(0);

                        // Use initialOdometer as baseline if it's the first set of logs being reported
                        int baselineOdometer = (vehicle.getInitialOdometer() != null
                                        && vehicle.getInitialOdometer() < minOdometerInLogs)
                                                        ? vehicle.getInitialOdometer()
                                                        : minOdometerInLogs;

                        int maxOdometer = logs.stream()
                                        .filter(l -> l.getOdometer() != null)
                                        .mapToInt(FuelLog::getOdometer)
                                        .max().orElse(baselineOdometer);
                        totalDistance = maxOdometer - baselineOdometer;
                }

                double avgCostPerKm = totalDistance > 0 ? totalCost / totalDistance : 0;

                return ExpenseReportDTO.builder()
                                .period(period)
                                .data(dataPoints)
                                .summary(ExpenseReportDTO.Summary.builder()
                                                .totalCost(totalCost)
                                                .totalFuel(totalFuel)
                                                .maintenanceCost(totalMaintenanceCost)
                                                .totalDistance(totalDistance)
                                                .averageCostPerKm(totalDistance > 0 ? totalCost / totalDistance : 0)
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
