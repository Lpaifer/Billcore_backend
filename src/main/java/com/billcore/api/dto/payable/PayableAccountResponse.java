package com.billcore.api.dto.payable;

import com.billcore.domain.enums.PayableAccountStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PayableAccountResponse(
    UUID id,
    String description,
    BigDecimal originalAmount,
    LocalDate dueDate,
    PayableAccountStatus status,
    String notes,
    UUID financialProfileId,
    UUID categoryId,
    UUID supplierId,
    LocalDate issueDate,
    LocalDate competenceDate,
    LocalDateTime createdAt
) {
}

