package com.vms.repository;

import com.vms.entity.Meter;
import com.vms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeterRepository extends JpaRepository<Meter, UUID> {
    Optional<Meter> findByIdentifier(String identifier);

    List<Meter> findByOwner(User owner);

    @Query("SELECT m FROM Meter m LEFT JOIN m.authorizedUsers a WHERE m.owner = :user OR (a.user = :user AND a.accessStatus = 'APPROVED')")
    List<Meter> findAllMetersAccessibleByUser(@Param("user") User user);

    boolean existsByIdentifier(String identifier);
}
