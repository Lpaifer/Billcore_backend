package com.billcore.domain.repository;

import com.billcore.domain.entity.PayableAccount;
import com.billcore.domain.enums.PayableAccountStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PayableAccountRepository extends JpaRepository<PayableAccount, UUID>, JpaSpecificationExecutor<PayableAccount> {
    List<PayableAccount> findByStatusAndDueDateBefore(PayableAccountStatus status, LocalDate dueDate);
    List<PayableAccount> findByStatus(PayableAccountStatus status);
    List<PayableAccount> findByStatusAndDueDateBetween(PayableAccountStatus status, LocalDate startDate, LocalDate endDate);
    List<PayableAccount> findByFinancialProfileId(UUID financialProfileId);
    List<PayableAccount> findByFinancialProfileIdAndStatusAndDueDateBetween(
        UUID financialProfileId,
        PayableAccountStatus status,
        LocalDate startDate,
        LocalDate endDate
    );
    Optional<PayableAccount> findByIdAndFinancialProfileId(UUID id, UUID financialProfileId);
}
