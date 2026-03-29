package com.lifepulse.repository;

import com.lifepulse.entity.VehicleAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleAccessRepository extends JpaRepository<VehicleAccess, UUID> {
    List<VehicleAccess> findByVehicleId(UUID vehicleId);

    List<VehicleAccess> findByUserIdAndAccessStatus(UUID userId, VehicleAccess.AccessStatus accessStatus);

    Optional<VehicleAccess> findByUserIdAndVehicleId(UUID userId, UUID vehicleId);

    List<VehicleAccess> findByVehicleUserIdAndAccessStatus(UUID ownerId, VehicleAccess.AccessStatus accessStatus);
}
