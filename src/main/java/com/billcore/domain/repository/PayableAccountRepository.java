package com.billcore.domain.repository;

import com.billcore.domain.entity.PayableAccount;
import com.billcore.domain.enums.PayableAccountStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayableAccountRepository extends JpaRepository<PayableAccount, UUID> {
    List<PayableAccount> findByStatusAndDueDateBefore(PayableAccountStatus status, LocalDate dueDate);
    List<PayableAccount> findByFinancialProfileId(UUID financialProfileId);
    Optional<PayableAccount> findByIdAndFinancialProfileId(UUID id, UUID financialProfileId);
}
