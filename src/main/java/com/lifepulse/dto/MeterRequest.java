package com.lifepulse.dto;

import lombok.Data;

@Data
public class MeterRequest {
    private String name;
    private String identifier;
    private String description;
}
