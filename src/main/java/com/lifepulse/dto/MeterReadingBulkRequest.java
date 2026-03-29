package com.lifepulse.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class MeterReadingBulkRequest {
    private UUID meterId;
    private Double readingValue;
    private String readingDate; // ISO date string
    private String recordedByManual;
    private String notes;
}
