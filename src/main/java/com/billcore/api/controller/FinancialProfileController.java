package com.billcore.api.controller;

import com.billcore.api.dto.financialprofile.FinancialProfileCreateRequest;
import com.billcore.api.dto.financialprofile.FinancialProfileResponse;
import com.billcore.domain.entity.User;
import com.billcore.domain.service.AuthService;
import com.billcore.domain.service.FinancialProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/financial-profiles")
@Tag(name = "Financial Profiles", description = "Perfil financeiro do usuario")
public class FinancialProfileController {

    private final AuthService authService;
    private final FinancialProfileService financialProfileService;

    public FinancialProfileController(AuthService authService, FinancialProfileService financialProfileService) {
        this.authService = authService;
        this.financialProfileService = financialProfileService;
    }

    @GetMapping("/health")
    @Operation(summary = "Health check do controller de perfis financeiros")
    public Map<String, String> health() {
        return Map.of("controller", "FinancialProfileController", "status", "ok");
    }

    @GetMapping
    @Operation(summary = "Lista perfis financeiros do usuario autenticado")
    public List<FinancialProfileResponse> list(Authentication authentication) {
        User user = authService.getRequiredActiveUserByEmail(authentication.getName());
        return financialProfileService.listForUser(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria perfil financeiro para o usuario autenticado")
    public FinancialProfileResponse create(
        @Valid @RequestBody FinancialProfileCreateRequest request,
        Authentication authentication
    ) {
        User user = authService.getRequiredActiveUserByEmail(authentication.getName());
        return financialProfileService.createForUser(user, request);
    }
}
