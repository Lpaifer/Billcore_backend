package com.billcore.domain.service.notification;

import com.billcore.domain.entity.PayableAccount;
import com.billcore.domain.enums.NotificationType;
import com.billcore.domain.enums.PayableAccountStatus;
import com.billcore.domain.repository.PayableAccountRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DueDateNotificationGenerationStrategy implements NotificationGenerationStrategy {

    private final PayableAccountRepository payableAccountRepository;

    public DueDateNotificationGenerationStrategy(PayableAccountRepository payableAccountRepository) {
        this.payableAccountRepository = payableAccountRepository;
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.DUE_DATE;
    }

    @Override
    public List<PayableAccount> findCandidateAccounts(LocalDate today, int daysAhead) {
        LocalDate endDate = today.plusDays(Math.max(0, daysAhead));
        return payableAccountRepository.findByStatusAndDueDateBetween(
            PayableAccountStatus.PENDING,
            today,
            endDate
        );
    }

    @Override
    public String buildTitle(PayableAccount account, LocalDate today) {
        long daysUntilDue = ChronoUnit.DAYS.between(today, account.getDueDate());
        if (daysUntilDue <= 0) {
            return "Conta vence hoje";
        }
        if (daysUntilDue == 1) {
            return "Conta vence amanha";
        }
        return "Conta vence em " + daysUntilDue + " dias";
    }

    @Override
    public String buildMessage(PayableAccount account, LocalDate today) {
        return "A conta \"" + account.getDescription() + "\" vence em " + account.getDueDate() + ".";
    }
}

