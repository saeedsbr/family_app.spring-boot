package com.lifepulse.dto;

import com.lifepulse.entity.Committee.CommitteeFrequency;
import com.lifepulse.entity.Committee.CommitteeStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@Builder
public class CommitteeResponse {
    private UUID id;
    private String name;
    private String currency;
    private int totalMembers;
    private BigDecimal amountPerMember;
    private CommitteeFrequency frequency;
    private int totalCycles;
    private int currentCycle;
    private LocalDate startDate;
    private CommitteeStatus status;
    private UserResponse createdBy;
    private LocalDateTime createdAt;
    private List<CommitteeMemberResponse> members;
}
