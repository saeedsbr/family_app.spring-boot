package com.lifepulse.dto;

import com.lifepulse.entity.VehicleAccess;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleAccessResponse {
    private UUID id;
    private String userName;
    private String userEmail;
    private UUID vehicleId;
    private String vehicleName;
    private String vehicleLicensePlate;
    private VehicleAccess.AccessStatus status;
    private LocalDateTime createdAt;
}
