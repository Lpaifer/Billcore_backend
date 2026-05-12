package com.billcore.domain.service.auth;

import com.billcore.domain.entity.User;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
public class ActiveUserPolicy {

    public void assertActive(User user, String inactiveMessage) {
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadCredentialsException(inactiveMessage);
        }
    }
}

