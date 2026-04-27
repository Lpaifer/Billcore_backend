package com.billcore.api.controller;

import com.billcore.api.dto.notification.NotificationResponse;
import com.billcore.domain.entity.User;
import com.billcore.domain.service.AuthService;
import com.billcore.domain.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notificacoes do usuario")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    public NotificationController(NotificationService notificationService, AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("controller", "NotificationController", "status", "ok");
    }

    @GetMapping
    @Operation(summary = "Lista notificacoes do usuario autenticado")
    public List<NotificationResponse> list(Authentication authentication) {
        User user = authService.getRequiredActiveUserByEmail(authentication.getName());
        return notificationService.list(user);
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marca notificacao como lida")
    public NotificationResponse markAsRead(
        @PathVariable UUID id,
        Authentication authentication
    ) {
        User user = authService.getRequiredActiveUserByEmail(authentication.getName());
        return notificationService.markAsRead(user, id);
    }
}
