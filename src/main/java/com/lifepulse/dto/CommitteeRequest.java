package com.lifepulse.dto;

import com.lifepulse.entity.Committee.CommitteeFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CommitteeRequest {
    @NotBlank
    private String name;

    private String currency = "PKR";

    @NotNull
    private Integer totalMembers;

    @NotNull
    private BigDecimal amountPerMember;

    @NotNull
    private CommitteeFrequency frequency;

    // The date Cycle 1 begins; subsequent cycle dates are derived from this + frequency
    private LocalDate startDate;

    // Optional initial members and their fixed rotation order (by email)
    private List<String> memberEmails;
}
