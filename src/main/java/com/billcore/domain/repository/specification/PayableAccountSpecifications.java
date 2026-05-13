package com.billcore.domain.repository.specification;

import com.billcore.domain.entity.PayableAccount;
import com.billcore.domain.enums.PayableAccountStatus;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class PayableAccountSpecifications {

    private PayableAccountSpecifications() {
    }

    public static Specification<PayableAccount> byFilters(
        UUID financialProfileId,
        PayableAccountStatus status,
        UUID categoryId,
        LocalDate dueDateFrom,
        LocalDate dueDateTo
    ) {
        return Specification.allOf(
            hasFinancialProfile(financialProfileId),
            hasStatus(status),
            hasCategory(categoryId),
            dueDateFrom(dueDateFrom),
            dueDateTo(dueDateTo)
        );
    }

    private static Specification<PayableAccount> hasFinancialProfile(UUID financialProfileId) {
        return (root, query, cb) -> cb.equal(root.get("financialProfile").get("id"), financialProfileId);
    }

    private static Specification<PayableAccount> hasStatus(PayableAccountStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<PayableAccount> hasCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    private static Specification<PayableAccount> dueDateFrom(LocalDate dueDateFrom) {
        if (dueDateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom);
    }

    private static Specification<PayableAccount> dueDateTo(LocalDate dueDateTo) {
        if (dueDateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dueDate"), dueDateTo);
    }
}

