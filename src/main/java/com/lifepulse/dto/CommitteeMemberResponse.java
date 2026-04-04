package com.lifepulse.dto;

import com.lifepulse.entity.CommitteeMember.MemberRole;
import com.lifepulse.entity.CommitteeMember.MemberStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CommitteeMemberResponse {
    private UUID id;
    private UserResponse user;
    private String customName;
    private int turnCycle;
    private boolean hasReceivedPot;
    private MemberRole role;
    private MemberStatus status;
    private LocalDateTime joinedAt;
}
