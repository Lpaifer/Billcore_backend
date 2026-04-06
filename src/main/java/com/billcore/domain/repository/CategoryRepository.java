package com.billcore.domain.repository;

import com.billcore.domain.entity.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByIdAndFinancialProfileId(UUID id, UUID financialProfileId);

    List<Category> findByFinancialProfileIdOrderByCreatedAtAsc(UUID financialProfileId);
}
