package com.billcore.api.controller;

import com.billcore.api.dto.financialprofile.FinancialProfileCreateRequest;
import com.billcore.api.dto.financialprofile.FinancialProfileResponse;
import com.billcore.domain.entity.User;
import com.billcore.domain.service.AuthService;
import com.billcore.domain.service.FinancialProfileService;
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
public class FinancialProfileController {

    private final AuthService authService;
    private final FinancialProfileService financialProfileService;

    public FinancialProfileController(AuthService authService, FinancialProfileService financialProfileService) {
        this.authService = authService;
        this.financialProfileService = financialProfileService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("controller", "FinancialProfileController", "status", "ok");
    }

    @GetMapping
    public List<FinancialProfileResponse> list(Authentication authentication) {
        User user = authService.getRequiredActiveUserByEmail(authentication.getName());
        return financialProfileService.listForUser(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FinancialProfileResponse create(
        @Valid @RequestBody FinancialProfileCreateRequest request,
        Authentication authentication
    ) {
        User user = authService.getRequiredActiveUserByEmail(authentication.getName());
        return financialProfileService.createForUser(user, request);
    }
}
