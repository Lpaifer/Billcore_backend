package com.billcore.domain.service.notification;

import com.billcore.domain.entity.PayableAccount;
import com.billcore.domain.enums.NotificationType;
import java.time.LocalDate;
import java.util.List;

public interface NotificationGenerationStrategy {

    NotificationType getNotificationType();

    List<PayableAccount> findCandidateAccounts(LocalDate today, int daysAhead);

    String buildTitle(PayableAccount account, LocalDate today);

    String buildMessage(PayableAccount account, LocalDate today);
}

