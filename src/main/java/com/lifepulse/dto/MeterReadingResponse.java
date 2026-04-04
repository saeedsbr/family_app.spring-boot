package com.lifepulse.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MeterReadingResponse {
    private UUID id;
    private UUID meterId;
    private String meterName;
    private String meterIdentifier;
    private LocalDate readingDate;
    private BigDecimal readingValue;
    private BigDecimal consumption;
    private String notes;
    private String recordedByManual;
    private String recordedByName;
    private UUID recordedBy;
}
