package com.lifepulse.dto;

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
    private String currency;
    private Integer initialOdometer;
}
