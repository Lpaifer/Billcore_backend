package com.billcore.domain.service;

import com.billcore.api.dto.financialprofile.FinancialProfileCreateRequest;
import com.billcore.api.dto.financialprofile.FinancialProfileResponse;
import com.billcore.domain.entity.Category;
import com.billcore.domain.entity.FinancialProfile;
import com.billcore.domain.entity.User;
import com.billcore.domain.repository.CategoryRepository;
import com.billcore.domain.repository.FinancialProfileRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinancialProfileService {

    private final FinancialProfileRepository financialProfileRepository;
    private final CategoryRepository categoryRepository;

    public FinancialProfileService(
        FinancialProfileRepository financialProfileRepository,
        CategoryRepository categoryRepository
    ) {
        this.financialProfileRepository = financialProfileRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<FinancialProfileResponse> listForUser(User user) {
        return financialProfileRepository.findByUserEmailOrderByCreatedAtAsc(user.getEmail()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public FinancialProfileResponse createForUser(User user, FinancialProfileCreateRequest request) {
        FinancialProfile profile = new FinancialProfile();
        profile.setId(UUID.randomUUID());
        profile.setName(request.name().trim());
        profile.setDescription(request.description() == null ? null : request.description().trim());
        profile.setProfileType(request.profileType());
        profile.setUser(user);
        profile.setActive(true);

        FinancialProfile savedProfile = financialProfileRepository.save(profile);
        Category defaultCategory = createDefaultCategory(savedProfile);

        return toResponse(savedProfile, defaultCategory.getId());
    }

    private Category createDefaultCategory(FinancialProfile profile) {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Geral");
        category.setFinancialProfile(profile);
        category.setActive(true);
        return categoryRepository.save(category);
    }

    private FinancialProfileResponse toResponse(FinancialProfile profile) {
        UUID defaultCategoryId = categoryRepository.findByFinancialProfileIdOrderByCreatedAtAsc(profile.getId()).stream()
            .findFirst()
            .map(Category::getId)
            .orElse(null);
        return toResponse(profile, defaultCategoryId);
    }

    private FinancialProfileResponse toResponse(FinancialProfile profile, UUID defaultCategoryId) {
        return new FinancialProfileResponse(
            profile.getId(),
            profile.getName(),
            profile.getDescription(),
            profile.getProfileType(),
            Boolean.TRUE.equals(profile.getActive()),
            defaultCategoryId,
            profile.getCreatedAt()
        );
    }
}
