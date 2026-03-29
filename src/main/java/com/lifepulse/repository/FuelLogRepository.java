package com.lifepulse.repository;

import com.lifepulse.entity.FuelLog;
import com.lifepulse.entity.VehicleAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FuelLogRepository extends JpaRepository<FuelLog, UUID> {

        List<FuelLog> findByVehicleIdOrderByLogDateDesc(UUID vehicleId);

        java.util.Optional<FuelLog> findFirstByVehicleIdOrderByLogDateDesc(UUID vehicleId);

        List<FuelLog> findByVehicleIdAndLogDateBetweenOrderByLogDateAsc(
                        UUID vehicleId, LocalDateTime startDate, LocalDateTime endDate);

        @Query("SELECT f FROM FuelLog f " +
                        "WHERE f.vehicle.user.id = :userId " +
                        "OR f.vehicle.id IN (SELECT va.vehicle.id FROM VehicleAccess va WHERE va.user.id = :userId AND va.accessStatus = :status) "
                        +
                        "ORDER BY f.logDate DESC")
        List<FuelLog> findRecentLogsByUserId(@Param("userId") UUID userId,
                        @Param("status") VehicleAccess.AccessStatus status);

        @Query("SELECT COALESCE(SUM(f.totalCost), 0.0) FROM FuelLog f WHERE f.vehicle.user.id = :userId")
        Double sumTotalCostByOwnerId(@Param("userId") UUID userId);
}
