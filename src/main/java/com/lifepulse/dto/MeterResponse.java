package com.lifepulse.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class MeterResponse {
    private UUID id;
    private String name;
    private String identifier;
    private String description;
    private UUID ownerId;
    private boolean isOwner;
}
