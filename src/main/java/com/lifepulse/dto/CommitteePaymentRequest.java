package com.lifepulse.dto;

import com.lifepulse.entity.CommitteeTransaction.PaymentMethod;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CommitteePaymentRequest {
    @NotNull
    private UUID userId; // The user making the payment

    @NotNull
    private BigDecimal amount;

    @NotNull
    private PaymentMethod method;
}
