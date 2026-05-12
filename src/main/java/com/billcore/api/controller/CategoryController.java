package com.billcore.api.controller;

import com.billcore.api.dto.category.CategoryCreateRequest;
import com.billcore.api.dto.category.CategoryResponse;
import com.billcore.domain.exception.BusinessRuleViolationException;
import com.billcore.domain.exception.ResourceNotFoundException;
import com.billcore.domain.entity.Category;
import com.billcore.domain.entity.FinancialProfile;
import com.billcore.domain.entity.User;
import com.billcore.domain.repository.CategoryRepository;
import com.billcore.domain.repository.FinancialProfileRepository;
import com.billcore.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Categories", description = "Categorias por perfil financeiro")
public class CategoryController {

    private final AuthService authService;
    private final FinancialProfileRepository financialProfileRepository;
    private final CategoryRepository categoryRepository;

    public CategoryController(
        AuthService authService,
        FinancialProfileRepository financialProfileRepository,
        CategoryRepository categoryRepository
    ) {
        this.authService = authService;
        this.financialProfileRepository = financialProfileRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/categories/health")
    @Operation(summary = "Health check do controller de categorias")
    public Map<String, String> health() {
        return Map.of("controller", "CategoryController", "status", "ok");
    }

    @GetMapping("/financial-profiles/{financialProfileId}/categories")
    @Operation(summary = "Lista categorias do perfil financeiro autenticado")
    public List<CategoryResponse> listByFinancialProfile(
        @PathVariable UUID financialProfileId,
        Authentication authentication
    ) {
        User user = authService.getRequiredActiveUserByEmail(authentication.getName());
        financialProfileRepository.findByIdAndUserEmail(financialProfileId, user.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Financial profile not found for authenticated user"));

        return categoryRepository.findByFinancialProfileIdOrderByCreatedAtAsc(financialProfileId).stream()
            .map(category -> new CategoryResponse(category.getId(), category.getName()))
            .toList();
    }

    @PostMapping("/financial-profiles/{financialProfileId}/categories")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria categoria no perfil financeiro autenticado")
    public CategoryResponse create(
        @PathVariable UUID financialProfileId,
        @Valid @RequestBody CategoryCreateRequest request,
        Authentication authentication
    ) {
        User user = authService.getRequiredActiveUserByEmail(authentication.getName());
        FinancialProfile profile = financialProfileRepository.findByIdAndUserEmail(financialProfileId, user.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Financial profile not found for authenticated user"));

        String trimmedName = request.name().trim();
        if (categoryRepository.existsByFinancialProfileIdAndNameIgnoreCase(profile.getId(), trimmedName)) {
            throw new BusinessRuleViolationException("Category name already exists for this financial profile");
        }

        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setFinancialProfile(profile);
        category.setName(trimmedName);
        category.setActive(true);

        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved.getId(), saved.getName());
    }
}
