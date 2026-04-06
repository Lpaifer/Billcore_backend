package com.billcore.api.dto.financialprofile;

import com.billcore.domain.enums.ProfileType;
import java.time.LocalDateTime;
import java.util.UUID;

public record FinancialProfileResponse(
    UUID id,
    String name,
    String description,
    ProfileType profileType,
    boolean active,
    UUID defaultCategoryId,
    LocalDateTime createdAt
) {
}
