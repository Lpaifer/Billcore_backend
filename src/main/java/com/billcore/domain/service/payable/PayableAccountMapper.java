package com.billcore.domain.service.payable;

import com.billcore.api.dto.payable.PayableAccountResponse;
import com.billcore.domain.entity.PayableAccount;
import org.springframework.stereotype.Component;

@Component
public class PayableAccountMapper {

    public PayableAccountResponse toResponse(PayableAccount account) {
        return new PayableAccountResponse(
            account.getId(),
            account.getDescription(),
            account.getOriginalAmount(),
            account.getDueDate(),
            account.getStatus(),
            account.getNotes(),
            account.getFinancialProfile().getId(),
            account.getCategory().getId(),
            account.getSupplier() == null ? null : account.getSupplier().getId(),
            account.getIssueDate(),
            account.getCompetenceDate(),
            account.getCreatedAt()
        );
    }
}

