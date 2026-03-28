package com.vms.repository;

import com.vms.entity.Meter;
import com.vms.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {
    List<MeterReading> findByMeterOrderByReadingDateDesc(Meter meter);

    Optional<MeterReading> findFirstByMeterAndReadingDateLessThanOrderByReadingDateDesc(Meter meter, LocalDate date);

    Optional<MeterReading> findFirstByMeterOrderByReadingDateDesc(Meter meter);
}
