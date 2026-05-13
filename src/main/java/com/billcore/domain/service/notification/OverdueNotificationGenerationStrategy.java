package com.billcore.domain.service.notification;

import com.billcore.domain.entity.PayableAccount;
import com.billcore.domain.enums.NotificationType;
import com.billcore.domain.enums.PayableAccountStatus;
import com.billcore.domain.repository.PayableAccountRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OverdueNotificationGenerationStrategy implements NotificationGenerationStrategy {

    private final PayableAccountRepository payableAccountRepository;

    public OverdueNotificationGenerationStrategy(PayableAccountRepository payableAccountRepository) {
        this.payableAccountRepository = payableAccountRepository;
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.OVERDUE;
    }

    @Override
    public List<PayableAccount> findCandidateAccounts(LocalDate today, int daysAhead) {
        return payableAccountRepository.findByStatus(PayableAccountStatus.OVERDUE);
    }

    @Override
    public String buildTitle(PayableAccount account, LocalDate today) {
        return "Conta em atraso";
    }

    @Override
    public String buildMessage(PayableAccount account, LocalDate today) {
        return "A conta \"" + account.getDescription() + "\" esta em atraso desde " + account.getDueDate() + ".";
    }
}

