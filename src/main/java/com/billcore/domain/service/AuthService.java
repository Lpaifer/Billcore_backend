package com.billcore.domain.service;

import com.billcore.api.dto.auth.AuthResponse;
import com.billcore.api.dto.auth.AuthUserResponse;
import com.billcore.api.dto.auth.LoginRequest;
import com.billcore.api.dto.auth.RegisterRequest;
import com.billcore.domain.entity.User;
import com.billcore.domain.repository.UserRepository;
import com.billcore.security.JwtService;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthUserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setActive(true);

        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadCredentialsException("User is inactive");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(), toUserResponse(user));
    }

    public User getRequiredActiveUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("Authenticated user not found"));
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadCredentialsException("Authenticated user is inactive");
        }
        return user;
    }

    private AuthUserResponse toUserResponse(User user) {
        return new AuthUserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            Boolean.TRUE.equals(user.getActive()),
            user.getCreatedAt()
        );
    }
}

