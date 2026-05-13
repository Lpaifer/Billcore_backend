package com.billcore.domain.service.payable;

import com.billcore.domain.exception.OwnershipViolationException;
import com.billcore.domain.exception.ResourceNotFoundException;
import com.billcore.domain.entity.FinancialProfile;
import com.billcore.domain.entity.PayableAccount;
import com.billcore.domain.repository.FinancialProfileRepository;
import com.billcore.domain.repository.PayableAccountRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PayableAccountOwnershipGuard {

    private final FinancialProfileRepository financialProfileRepository;
    private final PayableAccountRepository payableAccountRepository;

    public PayableAccountOwnershipGuard(
        FinancialProfileRepository financialProfileRepository,
        PayableAccountRepository payableAccountRepository
    ) {
        this.financialProfileRepository = financialProfileRepository;
        this.payableAccountRepository = payableAccountRepository;
    }

    public FinancialProfile getOwnedProfile(UUID financialProfileId, String userEmail) {
        return financialProfileRepository.findByIdAndUserEmail(financialProfileId, userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Financial profile not found for authenticated user"));
    }

    public PayableAccount getOwnedAccount(UUID payableAccountId, String userEmail) {
        PayableAccount account = payableAccountRepository.findById(payableAccountId)
            .orElseThrow(() -> new ResourceNotFoundException("Payable account not found"));
        if (!account.getFinancialProfile().getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new OwnershipViolationException("Payable account does not belong to authenticated user");
        }
        return account;
    }
}
