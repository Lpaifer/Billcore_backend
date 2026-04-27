package com.billcore.api.dto.notification;

import com.billcore.domain.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    String title,
    String message,
    NotificationType notificationType,
    boolean isRead,
    UUID payableAccountId,
    LocalDateTime createdAt
) {
}
