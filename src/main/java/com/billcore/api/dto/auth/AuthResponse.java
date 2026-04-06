package com.billcore.api.dto.auth;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    AuthUserResponse user
) {
}

