package com.billcore.domain.entity;

import com.billcore.domain.enums.PayableAccountStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PayableAccountTest {

    @Test
    void shouldTransitionFromPendingToPaid() {
        PayableAccount account = new PayableAccount();
        account.setStatus(PayableAccountStatus.PENDING);

        account.markAsPaid();

        Assertions.assertEquals(PayableAccountStatus.PAID, account.getStatus());
    }

    @Test
    void shouldTransitionFromOverdueToPaid() {
        PayableAccount account = new PayableAccount();
        account.setStatus(PayableAccountStatus.OVERDUE);

        account.markAsPaid();

        Assertions.assertEquals(PayableAccountStatus.PAID, account.getStatus());
    }

    @Test
    void shouldTransitionFromPendingToOverdue() {
        PayableAccount account = new PayableAccount();
        account.setStatus(PayableAccountStatus.PENDING);

        account.markAsOverdue();

        Assertions.assertEquals(PayableAccountStatus.OVERDUE, account.getStatus());
    }

    @Test
    void shouldTransitionFromPendingToCanceled() {
        PayableAccount account = new PayableAccount();
        account.setStatus(PayableAccountStatus.PENDING);

        account.cancel();

        Assertions.assertEquals(PayableAccountStatus.CANCELED, account.getStatus());
    }

    @Test
    void shouldNotCancelPaidAccount() {
        PayableAccount account = new PayableAccount();
        account.setStatus(PayableAccountStatus.PAID);

        Assertions.assertThrows(IllegalStateException.class, account::cancel);
    }

    @Test
    void shouldNotMarkCanceledAccountAsPaid() {
        PayableAccount account = new PayableAccount();
        account.setStatus(PayableAccountStatus.CANCELED);

        Assertions.assertThrows(IllegalStateException.class, account::markAsPaid);
    }

    @Test
    void shouldDetectOverdueByDateAndStatus() {
        PayableAccount account = new PayableAccount();
        account.setStatus(PayableAccountStatus.PENDING);
        account.setDueDate(LocalDate.of(2026, 4, 4));

        Assertions.assertTrue(account.isOverdue(LocalDate.of(2026, 4, 5)));
    }
}

