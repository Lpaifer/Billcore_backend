package com.billcore.api.controller;

import com.billcore.api.dto.auth.AuthUserResponse;
import com.billcore.api.dto.auth.RegisterRequest;
import com.billcore.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Cadastro de usuarios")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Cadastra novo usuario",
        security = @SecurityRequirement(name = "")
    )
    public AuthUserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
