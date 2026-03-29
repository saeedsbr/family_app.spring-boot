package com.lifepulse.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MeterAccessResponse {
    private UUID id;
    private UUID meterId;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String status;
    private LocalDateTime createdAt;
}
