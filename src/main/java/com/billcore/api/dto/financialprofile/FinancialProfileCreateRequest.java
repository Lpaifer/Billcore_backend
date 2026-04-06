package com.billcore.api.dto.financialprofile;

import com.billcore.domain.enums.ProfileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FinancialProfileCreateRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 255) String description,
    @NotNull ProfileType profileType
) {
}
