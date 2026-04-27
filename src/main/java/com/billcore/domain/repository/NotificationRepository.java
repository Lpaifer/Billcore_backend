package com.billcore.domain.repository;

import com.billcore.domain.entity.Notification;
import com.billcore.domain.enums.NotificationType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserEmailOrderByCreatedAtDesc(String email);
    boolean existsByUserIdAndPayableAccountIdAndNotificationType(
        UUID userId,
        UUID payableAccountId,
        NotificationType notificationType
    );
}
