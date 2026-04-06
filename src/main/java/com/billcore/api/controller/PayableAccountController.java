package com.billcore.api.controller;

import com.billcore.api.dto.payable.PayableAccountResponse;
import com.billcore.api.dto.payable.PayableAccountUpsertRequest;
import com.billcore.domain.entity.User;
import com.billcore.domain.enums.PayableAccountStatus;
import com.billcore.domain.service.AuthService;
import com.billcore.domain.service.PayableAccountService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PayableAccountController {

    private final PayableAccountService payableAccountService;
    private final AuthService authService;

    public PayableAccountController(PayableAccountService payableAccountService, AuthService authService) {
        this.payableAccountService = payableAccountService;
        this.authService = authService;
    }

    @GetMapping("/financial-profiles/{financialProfileId}/payable-accounts")
    public List<PayableAccountResponse> list(
        @PathVariable UUID financialProfileId,
        @RequestParam(required = false) PayableAccountStatus status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
        Authentication authentication
    ) {
        User user = authService.getRequiredActiveUserByEmail(authentication.getName());
        return payableAccountService.list(user, financialProfileId, status, dueDateFrom, dueDateTo);
    }

    @PostMapping("/financial-profiles/{financialProfileId}/payable-accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public PayableAccountResponse create(
        @PathVariable UUID financialProfileId,
        @Valid @RequestBody PayableAccountUpsertRequest request,
        Authentication authentication
    ) {
        User user = authService.getRequiredActiveUserByEmail(authentication.getName());
        return payableAccountService.create(user, financialProfileId, request);
    }

    @PatchMapping("/payable-accounts/{id}")
    public PayableAccountResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody PayableAccountUpsertRequest request,
        Authentication authentication
    ) {
        User user = authService.getRequiredActiveUserByEmail(authentication.getName());
        return payableAccountService.update(user, id, request);
    }

    @DeleteMapping("/payable-accounts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, Authentication authentication) {
        User user = authService.getRequiredActiveUserByEmail(authentication.getName());
        payableAccountService.delete(user, id);
    }

    @PatchMapping("/payable-accounts/{id}/cancel")
    public PayableAccountResponse cancel(@PathVariable UUID id, Authentication authentication) {
        User user = authService.getRequiredActiveUserByEmail(authentication.getName());
        return payableAccountService.cancel(user, id);
    }
}
