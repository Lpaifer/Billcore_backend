package com.billcore.domain.service;

import com.billcore.api.dto.auth.AuthResponse;
import com.billcore.api.dto.auth.AuthUserResponse;
import com.billcore.api.dto.auth.LoginRequest;
import com.billcore.api.dto.auth.RegisterRequest;
import com.billcore.domain.exception.BusinessRuleViolationException;
import com.billcore.domain.entity.User;
import com.billcore.domain.repository.UserRepository;
import com.billcore.domain.service.auth.ActiveUserPolicy;
import com.billcore.domain.service.auth.AuthCredentialsPolicy;
import com.billcore.domain.service.auth.AuthUserMapper;
import com.billcore.security.JwtService;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthCredentialsPolicy credentialsPolicy;
    private final ActiveUserPolicy activeUserPolicy;
    private final AuthUserMapper authUserMapper;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        AuthCredentialsPolicy credentialsPolicy,
        ActiveUserPolicy activeUserPolicy,
        AuthUserMapper authUserMapper,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.credentialsPolicy = credentialsPolicy;
        this.activeUserPolicy = activeUserPolicy;
        this.authUserMapper = authUserMapper;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthUserResponse register(RegisterRequest request) {
        String normalizedEmail = credentialsPolicy.normalizeEmail(request.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessRuleViolationException("Email already in use");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(credentialsPolicy.encodePassword(request.password()));
        user.setActive(true);

        User saved = userRepository.save(user);
        return authUserMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = credentialsPolicy.normalizeEmail(request.email());
        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        credentialsPolicy.assertPasswordMatches(request.password(), user.getPassword());
        activeUserPolicy.assertActive(user, "User is inactive");

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(), authUserMapper.toResponse(user));
    }

    public User getRequiredActiveUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("Authenticated user not found"));
        activeUserPolicy.assertActive(user, "Authenticated user is inactive");
        return user;
    }

}
