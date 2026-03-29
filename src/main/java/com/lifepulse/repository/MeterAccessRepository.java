package com.lifepulse.repository;

import com.lifepulse.entity.Meter;
import com.lifepulse.entity.MeterAccess;
import com.lifepulse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeterAccessRepository extends JpaRepository<MeterAccess, UUID> {
    List<MeterAccess> findByMeterAndAccessStatus(Meter meter, MeterAccess.AccessStatus status);

    List<MeterAccess> findByMeterIdAndAccessStatus(UUID meterId, MeterAccess.AccessStatus status);

    List<MeterAccess> findByUserAndAccessStatus(User user, MeterAccess.AccessStatus status);

    List<MeterAccess> findByMeterOwnerIdAndAccessStatus(UUID ownerId, MeterAccess.AccessStatus status);

    Optional<MeterAccess> findByMeterAndUser(Meter meter, User user);

    Optional<MeterAccess> findByUserIdAndMeterId(UUID userId, UUID meterId);

    boolean existsByMeterAndUser(Meter meter, User user);

}
