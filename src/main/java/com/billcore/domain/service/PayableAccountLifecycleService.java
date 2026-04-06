package com.billcore.domain.service;

import com.billcore.domain.entity.PayableAccount;
import com.billcore.domain.enums.PayableAccountStatus;
import com.billcore.domain.repository.PayableAccountRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayableAccountLifecycleService {

    private final PayableAccountRepository payableAccountRepository;

    public PayableAccountLifecycleService(PayableAccountRepository payableAccountRepository) {
        this.payableAccountRepository = payableAccountRepository;
    }

    @Transactional
    public void markAsPaid(PayableAccount payableAccount) {
        payableAccount.markAsPaid();
        payableAccountRepository.save(payableAccount);
    }

    @Transactional
    public void cancel(PayableAccount payableAccount) {
        payableAccount.cancel();
        payableAccountRepository.save(payableAccount);
    }

    @Transactional
    public int updateStatusToOverdue(LocalDate today) {
        List<PayableAccount> pendingAccounts = payableAccountRepository.findByStatusAndDueDateBefore(
            PayableAccountStatus.PENDING,
            today
        );

        pendingAccounts.forEach(PayableAccount::markAsOverdue);
        payableAccountRepository.saveAll(pendingAccounts);
        return pendingAccounts.size();
    }
}

