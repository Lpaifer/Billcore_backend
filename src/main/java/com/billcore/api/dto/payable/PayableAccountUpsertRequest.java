package com.billcore.api.dto.payable;

import com.billcore.domain.enums.PayableAccountStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PayableAccountUpsertRequest(
    @NotBlank @Size(max = 150) String description,
    @NotNull @DecimalMin(value = "0.01") BigDecimal originalAmount,
    @NotNull LocalDate dueDate,
    @NotNull UUID categoryId,
    UUID supplierId,
    LocalDate issueDate,
    LocalDate competenceDate,
    @Size(max = 5000) String notes,
    PayableAccountStatus status
) {
}
