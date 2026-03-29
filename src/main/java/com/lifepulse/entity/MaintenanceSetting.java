package com.lifepulse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "maintenance_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private Integer intervalKm;
    private LocalDateTime lastService;
    private LocalDateTime nextService;
}
