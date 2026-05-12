package com.billcore.domain.service.auth;

import com.billcore.api.dto.auth.AuthUserResponse;
import com.billcore.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthUserMapper {

    public AuthUserResponse toResponse(User user) {
        return new AuthUserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            Boolean.TRUE.equals(user.getActive()),
            user.getCreatedAt()
        );
    }
}

