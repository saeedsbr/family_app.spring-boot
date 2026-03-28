package com.vms.dto;

import io.micrometer.common.lang.Nullable;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class VehicleResponse {
    private UUID id;
    private String name;
    private String brand;
    private String model;
    private Integer modelYear;
    private String licensePlate;
    private Integer currentOdometer;
    private Integer lastServiceOdometer;
    private MaintenanceStatus maintenanceStatus;
}
