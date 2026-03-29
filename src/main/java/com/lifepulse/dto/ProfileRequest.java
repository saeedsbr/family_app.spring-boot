package com.lifepulse.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileRequest {
    @NotBlank
    private String name;

    private String currency;

    private String logoUrl;
}
