package com.vms.service;

import com.vms.dto.MaintenanceStatus;
import com.vms.dto.MaintenanceStatusType;
import com.vms.entity.Vehicle;
import com.vms.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaintenanceServiceTest {

    private MaintenanceService maintenanceService;

    @Mock
    private VehicleRepository vehicleRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        maintenanceService = new MaintenanceService(vehicleRepository);
    }

    @Test
    void testCalculateMaintenanceStatus_Healthy() {
        Vehicle vehicle = Vehicle.builder()
                .lastServiceOdometer(10000)
                .currentOdometer(12000)
                .build();

        MaintenanceStatus status = maintenanceService.calculateMaintenanceStatus(vehicle);

        assertEquals(MaintenanceStatusType.HEALTHY, status.getStatus());
        assertEquals(3000, status.getKmRemaining());
        assertEquals(2000, status.getKmSinceLastService());
        assertEquals(40.0, status.getProgressPercentage());
    }

    @Test
    void testCalculateMaintenanceStatus_DueSoon() {
        Vehicle vehicle = Vehicle.builder()
                .lastServiceOdometer(10000)
                .currentOdometer(14800)
                .build();

        MaintenanceStatus status = maintenanceService.calculateMaintenanceStatus(vehicle);

        assertEquals(MaintenanceStatusType.DUE_SOON, status.getStatus());
        assertEquals(200, status.getKmRemaining());
    }

    @Test
    void testCalculateMaintenanceStatus_Overdue() {
        Vehicle vehicle = Vehicle.builder()
                .lastServiceOdometer(10000)
                .currentOdometer(15100)
                .build();

        MaintenanceStatus status = maintenanceService.calculateMaintenanceStatus(vehicle);

        assertEquals(MaintenanceStatusType.OVERDUE, status.getStatus());
        assertEquals(0, status.getKmRemaining());
    }
}
