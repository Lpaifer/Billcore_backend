package com.billcore.jobs;

import com.billcore.domain.service.PayableAccountLifecycleService;
import com.billcore.domain.service.NotificationService;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BillcoreAutomationJobs {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillcoreAutomationJobs.class);

    private final PayableAccountLifecycleService payableAccountLifecycleService;
    private final NotificationService notificationService;

    public BillcoreAutomationJobs(
        PayableAccountLifecycleService payableAccountLifecycleService,
        NotificationService notificationService
    ) {
        this.payableAccountLifecycleService = payableAccountLifecycleService;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void updatePayableAccountStatusToOverdue() {
        int updated = payableAccountLifecycleService.updateStatusToOverdue(LocalDate.now());
        LOGGER.info("Overdue status update executed. Accounts updated: {}", updated);
    }

    @Scheduled(cron = "0 30 1 * * *")
    public void generateRecurringPayableAccounts() {
        LOGGER.info("Recurring payable account generation job executed.");
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void generateDueAndOverdueNotifications() {
        int generated = notificationService.generateDueAndOverdueNotifications(LocalDate.now(), 7);
        LOGGER.info("Due and overdue notifications generation job executed. Notifications created: {}", generated);
    }
}
