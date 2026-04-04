package com.lifepulse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "committees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Committee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    private String currency = "PKR";

    @Column(nullable = false)
    private int totalMembers;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amountPerMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommitteeFrequency frequency;

    @Column(nullable = false)
    private int totalCycles;

    // The date when Cycle 1 starts; subsequent cycles are calculated from this
    private LocalDate startDate;

    @Column(nullable = false)
    @Builder.Default
    private int currentCycle = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CommitteeStatus status = CommitteeStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @JsonIgnore
    @OneToMany(mappedBy = "committee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommitteeMember> members = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "committee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommitteeTransaction> transactions = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum CommitteeFrequency {
        DAILY, WEEKLY, BIWEEKLY, MONTHLY
    }

    public enum CommitteeStatus {
        ACTIVE, COMPLETED, DISPUTED
    }
}
