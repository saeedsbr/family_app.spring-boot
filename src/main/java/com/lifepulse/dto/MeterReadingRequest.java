package com.lifepulse.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MeterReadingRequest {
    private LocalDate readingDate;
    private BigDecimal readingValue;
    private String notes;
    private String recordedByManual;
}
