package com.lifepulse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private UUID id;
    private String email;
    private String name;
    private String currency;
    private String logoUrl;
}
