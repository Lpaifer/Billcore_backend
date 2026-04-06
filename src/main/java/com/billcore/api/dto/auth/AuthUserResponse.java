package com.billcore.api.dto.auth;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuthUserResponse(
    UUID id,
    String name,
    String email,
    boolean active,
    LocalDateTime createdAt
) {
}

