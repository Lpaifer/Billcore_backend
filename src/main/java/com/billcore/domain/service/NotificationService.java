package com.billcore.domain.service;

import com.billcore.api.dto.notification.NotificationResponse;
import com.billcore.domain.entity.Notification;
import com.billcore.domain.entity.PayableAccount;
import com.billcore.domain.entity.User;
import com.billcore.domain.enums.NotificationType;
import com.billcore.domain.enums.PayableAccountStatus;
import com.billcore.domain.repository.NotificationRepository;
import com.billcore.domain.repository.PayableAccountRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PayableAccountRepository payableAccountRepository;

    public NotificationService(
        NotificationRepository notificationRepository,
        PayableAccountRepository payableAccountRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.payableAccountRepository = payableAccountRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(User authenticatedUser) {
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(authenticatedUser.getEmail()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(User authenticatedUser, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getUser().getEmail().equalsIgnoreCase(authenticatedUser.getEmail())) {
            throw new IllegalArgumentException("Notification does not belong to authenticated user");
        }

        notification.markAsRead();
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public int generateDueAndOverdueNotifications(LocalDate today, int daysAhead) {
        int totalGenerated = 0;
        totalGenerated += generateDueDateNotifications(today, daysAhead);
        totalGenerated += generateOverdueNotifications();
        return totalGenerated;
    }

    private int generateDueDateNotifications(LocalDate today, int daysAhead) {
        LocalDate endDate = today.plusDays(Math.max(0, daysAhead));
        List<PayableAccount> dueSoonAccounts = payableAccountRepository.findByStatusAndDueDateBetween(
            PayableAccountStatus.PENDING,
            today,
            endDate
        );

        int created = 0;
        for (PayableAccount account : dueSoonAccounts) {
            User owner = account.getFinancialProfile().getUser();
            long daysUntilDue = ChronoUnit.DAYS.between(today, account.getDueDate());
            String title = buildDueTitle(daysUntilDue);
            String message = "A conta \"" + account.getDescription() + "\" vence em "
                + account.getDueDate() + ".";

            boolean alreadyExists = notificationRepository.existsByUserIdAndPayableAccountIdAndNotificationType(
                owner.getId(),
                account.getId(),
                NotificationType.DUE_DATE
            );

            if (alreadyExists) {
                continue;
            }

            notificationRepository.save(
                buildNotification(owner, account, title, message, NotificationType.DUE_DATE)
            );
            created++;
        }
        return created;
    }

    private int generateOverdueNotifications() {
        List<PayableAccount> overdueAccounts = payableAccountRepository.findByStatus(PayableAccountStatus.OVERDUE);
        int created = 0;
        for (PayableAccount account : overdueAccounts) {
            User owner = account.getFinancialProfile().getUser();
            String title = "Conta em atraso";
            String message = "A conta \"" + account.getDescription() + "\" esta em atraso desde "
                + account.getDueDate() + ".";

            boolean alreadyExists = notificationRepository.existsByUserIdAndPayableAccountIdAndNotificationType(
                owner.getId(),
                account.getId(),
                NotificationType.OVERDUE
            );

            if (alreadyExists) {
                continue;
            }

            notificationRepository.save(
                buildNotification(owner, account, title, message, NotificationType.OVERDUE)
            );
            created++;
        }
        return created;
    }

    private String buildDueTitle(long daysUntilDue) {
        if (daysUntilDue <= 0) {
            return "Conta vence hoje";
        }
        if (daysUntilDue == 1) {
            return "Conta vence amanha";
        }
        return "Conta vence em " + daysUntilDue + " dias";
    }

    private Notification buildNotification(
        User owner,
        PayableAccount account,
        String title,
        String message,
        NotificationType type
    ) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(type);
        notification.setIsRead(false);
        notification.setUser(owner);
        notification.setPayableAccount(account);
        return notification;
    }

    private NotificationResponse toResponse(Notification notification) {
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
