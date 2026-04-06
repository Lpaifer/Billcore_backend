package com.billcore.domain.service;

import com.billcore.api.dto.payable.PayableAccountResponse;
import com.billcore.api.dto.payable.PayableAccountUpsertRequest;
import com.billcore.domain.entity.Category;
import com.billcore.domain.entity.FinancialProfile;
import com.billcore.domain.entity.PayableAccount;
import com.billcore.domain.entity.Supplier;
import com.billcore.domain.entity.User;
import com.billcore.domain.enums.PayableAccountStatus;
import com.billcore.domain.repository.CategoryRepository;
import com.billcore.domain.repository.FinancialProfileRepository;
import com.billcore.domain.repository.PayableAccountRepository;
import com.billcore.domain.repository.SupplierRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayableAccountService {

    private final PayableAccountRepository payableAccountRepository;
    private final FinancialProfileRepository financialProfileRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public PayableAccountService(
        PayableAccountRepository payableAccountRepository,
        FinancialProfileRepository financialProfileRepository,
        CategoryRepository categoryRepository,
        SupplierRepository supplierRepository
    ) {
        this.payableAccountRepository = payableAccountRepository;
        this.financialProfileRepository = financialProfileRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public List<PayableAccountResponse> list(
        User authenticatedUser,
        UUID financialProfileId,
        PayableAccountStatus status,
        LocalDate dueDateFrom,
        LocalDate dueDateTo
    ) {
        FinancialProfile profile = getOwnedProfile(financialProfileId, authenticatedUser.getEmail());
        return payableAccountRepository.findByFinancialProfileId(profile.getId()).stream()
            .filter(account -> status == null || account.getStatus() == status)
            .filter(account -> dueDateFrom == null || !account.getDueDate().isBefore(dueDateFrom))
            .filter(account -> dueDateTo == null || !account.getDueDate().isAfter(dueDateTo))
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public PayableAccountResponse create(
        User authenticatedUser,
        UUID financialProfileId,
        PayableAccountUpsertRequest request
    ) {
        FinancialProfile profile = getOwnedProfile(financialProfileId, authenticatedUser.getEmail());
        Category category = categoryRepository.findByIdAndFinancialProfileId(request.categoryId(), profile.getId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found for financial profile"));
        Supplier supplier = resolveSupplier(profile.getId(), request.supplierId());

        PayableAccount account = new PayableAccount();
        account.setId(UUID.randomUUID());
        account.setFinancialProfile(profile);
        account.setCategory(category);
        account.setSupplier(supplier);
        account.setDescription(request.description().trim());
        account.setOriginalAmount(request.originalAmount());
        account.setDueDate(request.dueDate());
        account.setStatus(PayableAccountStatus.PENDING);
        account.setNotes(request.notes());
        account.setIssueDate(request.issueDate());
        account.setCompetenceDate(request.competenceDate());
        applyCreateStatus(account, request.status());

        PayableAccount saved = payableAccountRepository.save(account);
        return toResponse(saved);
    }

    @Transactional
    public PayableAccountResponse update(
        User authenticatedUser,
        UUID payableAccountId,
        PayableAccountUpsertRequest request
    ) {
        PayableAccount account = getOwnedAccount(payableAccountId, authenticatedUser.getEmail());

        Category category = categoryRepository.findByIdAndFinancialProfileId(
                request.categoryId(),
                account.getFinancialProfile().getId()
            )
            .orElseThrow(() -> new IllegalArgumentException("Category not found for financial profile"));
        Supplier supplier = resolveSupplier(account.getFinancialProfile().getId(), request.supplierId());

        account.setDescription(request.description().trim());
        account.setOriginalAmount(request.originalAmount());
        account.setDueDate(request.dueDate());
        account.setCategory(category);
        account.setSupplier(supplier);
        account.setNotes(request.notes());
        account.setIssueDate(request.issueDate());
        account.setCompetenceDate(request.competenceDate());
        applyUpdateStatus(account, request.status());

        return toResponse(payableAccountRepository.save(account));
    }

    @Transactional
    public void delete(User authenticatedUser, UUID payableAccountId) {
        PayableAccount account = getOwnedAccount(payableAccountId, authenticatedUser.getEmail());
        payableAccountRepository.delete(account);
    }

    @Transactional
    public PayableAccountResponse cancel(User authenticatedUser, UUID payableAccountId) {
        PayableAccount account = getOwnedAccount(payableAccountId, authenticatedUser.getEmail());
        account.cancel();
        return toResponse(payableAccountRepository.save(account));
    }

    private FinancialProfile getOwnedProfile(UUID financialProfileId, String userEmail) {
        return financialProfileRepository.findByIdAndUserEmail(financialProfileId, userEmail)
            .orElseThrow(() -> new IllegalArgumentException("Financial profile not found for authenticated user"));
    }

    private PayableAccount getOwnedAccount(UUID payableAccountId, String userEmail) {
        PayableAccount account = payableAccountRepository.findById(payableAccountId)
            .orElseThrow(() -> new IllegalArgumentException("Payable account not found"));
        if (!account.getFinancialProfile().getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new IllegalArgumentException("Payable account does not belong to authenticated user");
        }
        return account;
    }

    private Supplier resolveSupplier(UUID financialProfileId, UUID supplierId) {
        if (supplierId == null) {
            return null;
        }
        return supplierRepository.findByIdAndFinancialProfileId(supplierId, financialProfileId)
            .orElseThrow(() -> new IllegalArgumentException("Supplier not found for financial profile"));
    }

    private void applyCreateStatus(PayableAccount account, PayableAccountStatus requestedStatus) {
        if (requestedStatus == null || requestedStatus == PayableAccountStatus.PENDING) {
            return;
        }
        if (requestedStatus == PayableAccountStatus.PAID) {
            account.markAsPaid();
            return;
        }
        throw new IllegalArgumentException("Only PENDING or PAID are allowed on account creation");
    }

    private void applyUpdateStatus(PayableAccount account, PayableAccountStatus requestedStatus) {
        if (requestedStatus == null || requestedStatus == account.getStatus()) {
            return;
        }

        switch (requestedStatus) {
            case PAID -> account.markAsPaid();
            case OVERDUE -> account.markAsOverdue();
            case CANCELED -> account.cancel();
            case PENDING -> throw new IllegalArgumentException("Reverting account status to PENDING is not allowed");
            default -> throw new IllegalArgumentException("Unsupported account status update");
        }
    }

    private PayableAccountResponse toResponse(PayableAccount account) {
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
