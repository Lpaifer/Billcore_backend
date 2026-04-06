package com.billcore.domain.repository;

import com.billcore.domain.entity.FinancialProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialProfileRepository extends JpaRepository<FinancialProfile, UUID> {
    Optional<FinancialProfile> findByIdAndUserEmail(UUID id, String email);

    List<FinancialProfile> findByUserEmailOrderByCreatedAtAsc(String email);
}
