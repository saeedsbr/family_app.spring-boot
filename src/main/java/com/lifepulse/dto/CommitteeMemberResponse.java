package com.lifepulse.dto;

import com.lifepulse.entity.CommitteeMember.MemberRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CommitteeMemberResponse {
    private UUID id;
    private UserResponse user;
    private int turnCycle;
    private boolean hasReceivedPot;
    private MemberRole role;
    private LocalDateTime joinedAt;
}
