package com.vms.dto;

import lombok.Data;

@Data
public class MeterRequest {
    private String name;
    private String identifier;
    private String description;
}
