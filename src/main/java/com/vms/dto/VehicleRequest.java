package com.vms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VehicleRequest {
    @NotBlank
    private String name;
    private String brand;
    private String model;
    private Integer modelYear;
    private String licensePlate;
    private Integer initialOdometer;
}
