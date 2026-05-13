package com.billcore.domain.service.payable;

import com.billcore.domain.exception.BusinessRuleViolationException;
import com.billcore.domain.entity.PayableAccount;
import com.billcore.domain.enums.PayableAccountStatus;
import org.springframework.stereotype.Component;

@Component
public class PayableAccountStatusPolicy {

    public void applyCreateStatus(PayableAccount account, PayableAccountStatus requestedStatus) {
        if (requestedStatus == null || requestedStatus == PayableAccountStatus.PENDING) {
            return;
        }
        if (requestedStatus == PayableAccountStatus.PAID) {
            account.markAsPaid();
            return;
        }
        throw new BusinessRuleViolationException("Only PENDING or PAID are allowed on account creation");
    }

    public void applyUpdateStatus(PayableAccount account, PayableAccountStatus requestedStatus) {
        if (requestedStatus == null || requestedStatus == account.getStatus()) {
            return;
        }

        switch (requestedStatus) {
            case PAID -> account.markAsPaid();
            case OVERDUE -> account.markAsOverdue();
            case CANCELED -> account.cancel();
            case PENDING -> throw new BusinessRuleViolationException("Reverting account status to PENDING is not allowed");
            default -> throw new BusinessRuleViolationException("Unsupported account status update");
        }
    }
}
