package com.lifepulse.dto;

import com.lifepulse.entity.CommitteeTransaction.PaymentMethod;
import com.lifepulse.entity.CommitteeTransaction.TransactionStatus;
import com.lifepulse.entity.CommitteeTransaction.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CommitteeTransactionResponse {
    private UUID id;
    private UserResponse fromUser;
    private String toRecipient;
    private BigDecimal amount;
    private TransactionType type;
    private int cycleNumber;
    private PaymentMethod method;
    private TransactionStatus status;
    private LocalDateTime paidAt;
}
