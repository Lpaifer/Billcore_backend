package com.billcore.domain.service;

import com.billcore.api.dto.notification.NotificationResponse;
import com.billcore.domain.exception.OwnershipViolationException;
import com.billcore.domain.exception.ResourceNotFoundException;
import com.billcore.domain.entity.Notification;
import com.billcore.domain.entity.PayableAccount;
import com.billcore.domain.entity.User;
import com.billcore.domain.enums.NotificationType;
import com.billcore.domain.repository.NotificationRepository;
import com.billcore.domain.service.notification.NotificationGenerationStrategy;
import com.billcore.domain.service.notification.NotificationMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final List<NotificationGenerationStrategy> generationStrategies;
    private final NotificationMapper notificationMapper;

    public NotificationService(
        NotificationRepository notificationRepository,
        List<NotificationGenerationStrategy> generationStrategies,
        NotificationMapper notificationMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.generationStrategies = generationStrategies;
        this.notificationMapper = notificationMapper;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(User authenticatedUser) {
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(authenticatedUser.getEmail()).stream()
            .map(notificationMapper::toResponse)
            .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(User authenticatedUser, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getEmail().equalsIgnoreCase(authenticatedUser.getEmail())) {
            throw new OwnershipViolationException("Notification does not belong to authenticated user");
        }

        notification.markAsRead();
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public int generateDueAndOverdueNotifications(LocalDate today, int daysAhead) {
        int totalGenerated = 0;
        for (NotificationGenerationStrategy strategy : generationStrategies) {
            totalGenerated += generateNotificationsForStrategy(strategy, today, daysAhead);
        }
        return totalGenerated;
    }

    private int generateNotificationsForStrategy(
        NotificationGenerationStrategy strategy,
        LocalDate today,
        int daysAhead
    ) {
        List<PayableAccount> accounts = strategy.findCandidateAccounts(today, daysAhead);
        int created = 0;
        for (PayableAccount account : accounts) {
            User owner = account.getFinancialProfile().getUser();
            boolean alreadyExists = notificationRepository.existsByUserIdAndPayableAccountIdAndNotificationType(
                owner.getId(),
                account.getId(),
                strategy.getNotificationType()
            );

            if (alreadyExists) {
                continue;
            }

            notificationRepository.save(
                buildNotification(
                    owner,
                    account,
                    strategy.buildTitle(account, today),
                    strategy.buildMessage(account, today),
                    strategy.getNotificationType()
                )
            );
            created++;
        }
        return created;
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

}
