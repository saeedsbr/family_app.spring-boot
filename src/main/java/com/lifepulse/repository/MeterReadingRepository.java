package com.lifepulse.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lifepulse.entity.Meter;
import com.lifepulse.entity.MeterReading;

@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {
    List<MeterReading> findByMeterOrderByReadingDateDesc(Meter meter);

    List<MeterReading> findByMeterInOrderByReadingDateDesc(List<Meter> meters);

    Optional<MeterReading> findFirstByMeterAndReadingDateLessThanOrderByReadingDateDesc(Meter meter, LocalDate date);

    Optional<MeterReading> findFirstByMeterOrderByReadingDateDesc(Meter meter);

    Optional<MeterReading> findFirstByMeterAndReadingDateGreaterThanOrderByReadingDateAsc(Meter meter, LocalDate date);
}
