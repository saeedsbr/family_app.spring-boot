package com.vms.repository;

import com.vms.entity.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, UUID> {
    List<MaintenanceLog> findByVehicleIdOrderByServiceDateDesc(UUID vehicleId);

    @Query("SELECT COALESCE(SUM(m.cost), 0.0) FROM MaintenanceLog m WHERE m.vehicle.user.id = :userId")
    Double sumCostByOwnerId(@Param("userId") UUID userId);
}
