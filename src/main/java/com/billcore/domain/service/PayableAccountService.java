package com.billcore.domain.service;

import com.billcore.api.dto.payable.PayableAccountResponse;
import com.billcore.api.dto.payable.PayableAccountUpsertRequest;
import com.billcore.domain.exception.BusinessRuleViolationException;
import com.billcore.domain.entity.Category;
import com.billcore.domain.entity.FinancialProfile;
import com.billcore.domain.entity.PayableAccount;
import com.billcore.domain.entity.Supplier;
import com.billcore.domain.entity.User;
import com.billcore.domain.enums.PayableAccountStatus;
import com.billcore.domain.repository.CategoryRepository;
import com.billcore.domain.repository.PayableAccountRepository;
import com.billcore.domain.repository.SupplierRepository;
import com.billcore.domain.repository.specification.PayableAccountSpecifications;
import com.billcore.domain.service.payable.PayableAccountMapper;
import com.billcore.domain.service.payable.PayableAccountOwnershipGuard;
import com.billcore.domain.service.payable.PayableAccountStatusPolicy;
import java.util.Comparator;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayableAccountService {

    private final PayableAccountRepository payableAccountRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final PayableAccountOwnershipGuard ownershipGuard;
    private final PayableAccountStatusPolicy statusPolicy;
    private final PayableAccountMapper mapper;

    public PayableAccountService(
        PayableAccountRepository payableAccountRepository,
        CategoryRepository categoryRepository,
        SupplierRepository supplierRepository,
        PayableAccountOwnershipGuard ownershipGuard,
        PayableAccountStatusPolicy statusPolicy,
        PayableAccountMapper mapper
    ) {
        this.payableAccountRepository = payableAccountRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.ownershipGuard = ownershipGuard;
        this.statusPolicy = statusPolicy;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<PayableAccountResponse> list(
        User authenticatedUser,
        UUID financialProfileId,
        PayableAccountStatus status,
        UUID categoryId,
        LocalDate dueDateFrom,
        LocalDate dueDateTo
    ) {
        if (dueDateFrom != null && dueDateTo != null && dueDateFrom.isAfter(dueDateTo)) {
            throw new BusinessRuleViolationException("dueDateFrom cannot be after dueDateTo");
        }

        FinancialProfile profile = ownershipGuard.getOwnedProfile(financialProfileId, authenticatedUser.getEmail());
        return payableAccountRepository.findAll(
                PayableAccountSpecifications.byFilters(
                    profile.getId(),
                    status,
                    categoryId,
                    dueDateFrom,
                    dueDateTo
                )
            ).stream()
            .sorted(byDueDateAscending())
            .map(mapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PayableAccountResponse> listDueSoon(
        User authenticatedUser,
        UUID financialProfileId,
        int daysAhead
    ) {
        FinancialProfile profile = ownershipGuard.getOwnedProfile(financialProfileId, authenticatedUser.getEmail());
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(Math.max(0, daysAhead));

        return payableAccountRepository.findByFinancialProfileIdAndStatusAndDueDateBetween(
                profile.getId(),
                PayableAccountStatus.PENDING,
                today,
                endDate
            ).stream()
            .sorted(byDueDateAscending())
            .map(mapper::toResponse)
            .toList();
    }

    @Transactional
    public PayableAccountResponse create(
        User authenticatedUser,
        UUID financialProfileId,
        PayableAccountUpsertRequest request
    ) {
        FinancialProfile profile = ownershipGuard.getOwnedProfile(financialProfileId, authenticatedUser.getEmail());
        Category category = categoryRepository.findByIdAndFinancialProfileId(request.categoryId(), profile.getId())
            .orElseThrow(() -> new BusinessRuleViolationException("Category not found for financial profile"));
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
        statusPolicy.applyCreateStatus(account, request.status());

        PayableAccount saved = payableAccountRepository.save(account);
        return mapper.toResponse(saved);
    }

    @Transactional
    public PayableAccountResponse update(
        User authenticatedUser,
        UUID payableAccountId,
        PayableAccountUpsertRequest request
    ) {
        PayableAccount account = ownershipGuard.getOwnedAccount(payableAccountId, authenticatedUser.getEmail());

        Category category = categoryRepository.findByIdAndFinancialProfileId(
                request.categoryId(),
                account.getFinancialProfile().getId()
            )
            .orElseThrow(() -> new BusinessRuleViolationException("Category not found for financial profile"));
        Supplier supplier = resolveSupplier(account.getFinancialProfile().getId(), request.supplierId());

        account.setDescription(request.description().trim());
        account.setOriginalAmount(request.originalAmount());
        account.setDueDate(request.dueDate());
        account.setCategory(category);
        account.setSupplier(supplier);
        account.setNotes(request.notes());
        account.setIssueDate(request.issueDate());
        account.setCompetenceDate(request.competenceDate());
        statusPolicy.applyUpdateStatus(account, request.status());

        return mapper.toResponse(payableAccountRepository.save(account));
    }

    @Transactional
    public void delete(User authenticatedUser, UUID payableAccountId) {
        PayableAccount account = ownershipGuard.getOwnedAccount(payableAccountId, authenticatedUser.getEmail());
        payableAccountRepository.delete(account);
    }

    @Transactional
    public PayableAccountResponse cancel(User authenticatedUser, UUID payableAccountId) {
        PayableAccount account = ownershipGuard.getOwnedAccount(payableAccountId, authenticatedUser.getEmail());
        account.cancel();
        return mapper.toResponse(payableAccountRepository.save(account));
    }

    private Supplier resolveSupplier(UUID financialProfileId, UUID supplierId) {
        if (supplierId == null) {
            return null;
        }
        return supplierRepository.findByIdAndFinancialProfileId(supplierId, financialProfileId)
            .orElseThrow(() -> new BusinessRuleViolationException("Supplier not found for financial profile"));
    }

    private Comparator<PayableAccount> byDueDateAscending() {
        return Comparator.comparing(PayableAccount::getDueDate)
            .thenComparing(PayableAccount::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
