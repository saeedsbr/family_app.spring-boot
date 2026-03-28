package com.vms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MaintenanceStatusType {
    HEALTHY("Healthy", "bg-green-500", "Your vehicle is in good condition"),
    DUE_SOON("Due Soon", "bg-yellow-500", "Service recommended within 500 km"),
    OVERDUE("Overdue", "bg-red-500", "Service is overdue!");

    private final String displayName;
    private final String colorClass;
    private final String message;
}
