package com.billcore.domain.repository;

import com.billcore.domain.entity.Supplier;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    Optional<Supplier> findByIdAndFinancialProfileId(UUID id, UUID financialProfileId);
}
