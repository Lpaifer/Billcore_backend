package com.billcore.domain.service.notification;

import com.billcore.api.dto.notification.NotificationResponse;
import com.billcore.domain.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getNotificationType(),
            Boolean.TRUE.equals(notification.getIsRead()),
            notification.getPayableAccount() == null ? null : notification.getPayableAccount().getId(),
            notification.getCreatedAt()
        );
    }
}

