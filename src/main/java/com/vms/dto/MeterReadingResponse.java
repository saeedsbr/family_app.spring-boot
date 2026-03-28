package com.vms.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class MeterReadingResponse {
    private UUID id;
    private UUID meterId;
    private LocalDate readingDate;
    private BigDecimal readingValue;
    private BigDecimal consumption;
    private String notes;
    private String recordedByName;
    private UUID recordedBy;
}
