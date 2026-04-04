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

    // How often members pay their contribution
    @NotNull
    private CommitteeFrequency frequency;

    // How often the pot is given to the next person in rotation
    @NotNull
    private CommitteeFrequency payoutFrequency;

    // The date Cycle 1 begins; subsequent cycle dates are derived from this + payoutFrequency
    private LocalDate startDate;

    // Optional initial members and their fixed rotation order (by email)
    private List<String> memberEmails;
}
