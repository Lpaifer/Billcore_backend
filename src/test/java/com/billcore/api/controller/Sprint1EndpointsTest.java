package com.billcore.api.controller;

import com.billcore.domain.entity.Category;
import com.billcore.domain.entity.FinancialProfile;
import com.billcore.domain.entity.User;
import com.billcore.domain.enums.ProfileType;
import com.billcore.domain.repository.CategoryRepository;
import com.billcore.domain.repository.FinancialProfileRepository;
import com.billcore.domain.repository.UserRepository;
import com.billcore.domain.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class Sprint1EndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FinancialProfileRepository financialProfileRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationService notificationService;

    @Test
    void shouldRegisterAndLoginWithEncryptedPassword() throws Exception {
        String registerPayload = """
            {
              "name": "Ana Sprint",
              "email": "ana.sprint@billcore.dev",
              "password": "SenhaSegura123"
            }
            """;

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("ana.sprint@billcore.dev"));

        User savedUser = userRepository.findByEmail("ana.sprint@billcore.dev").orElseThrow();
        Assertions.assertNotEquals("SenhaSegura123", savedUser.getPassword());
        Assertions.assertTrue(passwordEncoder.matches("SenhaSegura123", savedUser.getPassword()));

        String loginPayload = """
            {
              "email": "ana.sprint@billcore.dev",
              "password": "SenhaSegura123"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldRunPayableAccountCrudFlow() throws Exception {
        String email = "rf2.user@billcore.dev";
        String password = "SenhaSegura123";

        registerUser(email, password);
        String token = loginAndGetToken(email, password);

        User owner = userRepository.findByEmail(email).orElseThrow();
        FinancialProfile profile = new FinancialProfile();
        profile.setId(UUID.randomUUID());
        profile.setName("Perfil Sprint 1");
        profile.setProfileType(ProfileType.PERSONAL);
        profile.setUser(owner);
        profile.setActive(true);
        profile = financialProfileRepository.save(profile);

        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Moradia");
        category.setFinancialProfile(profile);
        category.setActive(true);
        category = categoryRepository.save(category);

        String createPayload = """
            {
              "description": "Conta de energia",
              "originalAmount": 189.90,
              "dueDate": "2026-04-25",
              "categoryId": "%s",
              "supplierId": null,
              "issueDate": "2026-04-01",
              "competenceDate": "2026-04-01",
              "notes": "Sprint 1 RF2"
            }
            """.formatted(category.getId());

        MvcResult createResult = mockMvc.perform(post("/api/v1/financial-profiles/{financialProfileId}/payable-accounts", profile.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String payableAccountId = created.get("id").asText();

        mockMvc.perform(get("/api/v1/financial-profiles/{financialProfileId}/payable-accounts", profile.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(payableAccountId));

        String updatePayload = """
            {
              "description": "Conta de energia atualizada",
              "originalAmount": 199.90,
              "dueDate": "2026-04-28",
              "categoryId": "%s",
              "supplierId": null,
              "issueDate": "2026-04-02",
              "competenceDate": "2026-04-01",
              "notes": "Update RF2"
            }
            """.formatted(category.getId());

        mockMvc.perform(patch("/api/v1/payable-accounts/{id}", payableAccountId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Conta de energia atualizada"));

        mockMvc.perform(patch("/api/v1/payable-accounts/{id}/cancel", payableAccountId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(delete("/api/v1/payable-accounts/{id}", payableAccountId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldCreateAndListFinancialProfilesForAuthenticatedUser() throws Exception {
        String email = "profile.user@billcore.dev";
        String password = "SenhaSegura123";

        registerUser(email, password);
        String token = loginAndGetToken(email, password);

        String createPayload = """
            {
              "name": "Perfil Principal",
              "description": "Perfil inicial para onboarding",
              "profileType": "PERSONAL"
            }
            """;

        MvcResult createResult = mockMvc.perform(post("/api/v1/financial-profiles")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Perfil Principal"))
            .andExpect(jsonPath("$.defaultCategoryId").isNotEmpty())
            .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String profileId = created.get("id").asText();

        mockMvc.perform(get("/api/v1/financial-profiles")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(profileId))
            .andExpect(jsonPath("$[0].defaultCategoryId").isNotEmpty());
    }

    @Test
    void shouldCreatePayableAsPaidAndListCategories() throws Exception {
        String email = "paid.account@billcore.dev";
        String password = "SenhaSegura123";

        registerUser(email, password);
        String token = loginAndGetToken(email, password);

        String createProfilePayload = """
            {
              "name": "Perfil Pagamento",
              "description": "Perfil para teste de conta paga",
              "profileType": "PERSONAL"
            }
            """;

        MvcResult profileResult = mockMvc.perform(post("/api/v1/financial-profiles")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createProfilePayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.defaultCategoryId").isNotEmpty())
            .andReturn();

        JsonNode profileJson = objectMapper.readTree(profileResult.getResponse().getContentAsString());
        String profileId = profileJson.get("id").asText();
        String categoryId = profileJson.get("defaultCategoryId").asText();

        mockMvc.perform(get("/api/v1/financial-profiles/{financialProfileId}/categories", profileId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(categoryId));

        String createPaidPayload = """
            {
              "description": "Conta ja paga",
              "originalAmount": 49.90,
              "dueDate": "2026-07-30",
              "categoryId": "%s",
              "supplierId": null,
              "issueDate": "2026-07-01",
              "competenceDate": "2026-07-01",
              "notes": "Criada como paga",
              "status": "PAID"
            }
            """.formatted(categoryId);

        mockMvc.perform(post("/api/v1/financial-profiles/{financialProfileId}/payable-accounts", profileId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPaidPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void shouldListDueSoonAndHandleNotifications() throws Exception {
        String email = "us3.user@billcore.dev";
        String password = "SenhaSegura123";

        registerUser(email, password);
        String token = loginAndGetToken(email, password);

        String createProfilePayload = """
            {
              "name": "Perfil US3",
              "description": "Perfil para contas a vencer",
              "profileType": "PERSONAL"
            }
            """;

        MvcResult profileResult = mockMvc.perform(post("/api/v1/financial-profiles")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createProfilePayload))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode profileJson = objectMapper.readTree(profileResult.getResponse().getContentAsString());
        String profileId = profileJson.get("id").asText();
        String categoryId = profileJson.get("defaultCategoryId").asText();

        String nearDueDate = LocalDate.now().plusDays(2).toString();
        String farDueDate = LocalDate.now().plusDays(12).toString();

        String dueSoonPayload = """
            {
              "description": "Conta internet",
              "originalAmount": 120.00,
              "dueDate": "%s",
              "categoryId": "%s",
              "supplierId": null,
              "issueDate": "2026-04-01",
              "competenceDate": "2026-04-01",
              "notes": "US3 due soon"
            }
            """.formatted(nearDueDate, categoryId);

        String paidSoonPayload = """
            {
              "description": "Conta de servico paga",
              "originalAmount": 89.00,
              "dueDate": "%s",
              "categoryId": "%s",
              "supplierId": null,
              "issueDate": "2026-04-01",
              "competenceDate": "2026-04-01",
              "notes": "US3 paid",
              "status": "PAID"
            }
            """.formatted(nearDueDate, categoryId);

        String canceledSoonPayload = """
            {
              "description": "Conta cancelada",
              "originalAmount": 39.00,
              "dueDate": "%s",
              "categoryId": "%s",
              "supplierId": null,
              "issueDate": "2026-04-01",
              "competenceDate": "2026-04-01",
              "notes": "US3 canceled"
            }
            """.formatted(nearDueDate, categoryId);

        String outsideWindowPayload = """
            {
              "description": "Conta anual",
              "originalAmount": 500.00,
              "dueDate": "%s",
              "categoryId": "%s",
              "supplierId": null,
              "issueDate": "2026-04-01",
              "competenceDate": "2026-04-01",
              "notes": "US3 fora da janela"
            }
            """.formatted(farDueDate, categoryId);

        MvcResult dueSoonAccountResult = mockMvc.perform(post("/api/v1/financial-profiles/{financialProfileId}/payable-accounts", profileId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dueSoonPayload))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode dueSoonAccountJson = objectMapper.readTree(dueSoonAccountResult.getResponse().getContentAsString());
        String dueSoonAccountId = dueSoonAccountJson.get("id").asText();

        mockMvc.perform(post("/api/v1/financial-profiles/{financialProfileId}/payable-accounts", profileId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(paidSoonPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PAID"));

        MvcResult canceledAccountResult = mockMvc.perform(post("/api/v1/financial-profiles/{financialProfileId}/payable-accounts", profileId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(canceledSoonPayload))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode canceledAccountJson = objectMapper.readTree(canceledAccountResult.getResponse().getContentAsString());
        String canceledAccountId = canceledAccountJson.get("id").asText();

        mockMvc.perform(patch("/api/v1/payable-accounts/{id}/cancel", canceledAccountId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(post("/api/v1/financial-profiles/{financialProfileId}/payable-accounts", profileId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(outsideWindowPayload))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/financial-profiles/{financialProfileId}/payable-accounts/due-soon?daysAhead=7", profileId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(dueSoonAccountId));

        int generatedFirstRun = notificationService.generateDueAndOverdueNotifications(LocalDate.now(), 7);
        int generatedSecondRun = notificationService.generateDueAndOverdueNotifications(LocalDate.now(), 7);

        Assertions.assertEquals(1, generatedFirstRun);
        Assertions.assertEquals(0, generatedSecondRun);

        MvcResult notificationsResult = mockMvc.perform(get("/api/v1/notifications")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].notificationType").value("DUE_DATE"))
            .andExpect(jsonPath("$[0].payableAccountId").value(dueSoonAccountId))
            .andExpect(jsonPath("$[0].isRead").value(false))
            .andReturn();

        JsonNode notificationsJson = objectMapper.readTree(notificationsResult.getResponse().getContentAsString());
        String notificationId = notificationsJson.get(0).get("id").asText();

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notificationId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(notificationId))
            .andExpect(jsonPath("$.isRead").value(true));
    }

    @Test
    void shouldFilterPayableAccountsByStatusCategoryAndDuePeriod() throws Exception {
        String email = "us5.user@billcore.dev";
        String password = "SenhaSegura123";

        registerUser(email, password);
        String token = loginAndGetToken(email, password);

        User owner = userRepository.findByEmail(email).orElseThrow();
        FinancialProfile profile = new FinancialProfile();
        profile.setId(UUID.randomUUID());
        profile.setName("Perfil US5");
        profile.setProfileType(ProfileType.PERSONAL);
        profile.setUser(owner);
        profile.setActive(true);
        profile = financialProfileRepository.save(profile);

        Category housing = new Category();
        housing.setId(UUID.randomUUID());
        housing.setName("Moradia US5");
        housing.setFinancialProfile(profile);
        housing.setActive(true);
        housing = categoryRepository.save(housing);

        Category food = new Category();
        food.setId(UUID.randomUUID());
        food.setName("Alimentacao US5");
        food.setFinancialProfile(profile);
        food.setActive(true);
        food = categoryRepository.save(food);

        String createHousingPending = """
            {
              "description": "Conta de internet",
              "originalAmount": 120.00,
              "dueDate": "2026-05-15",
              "categoryId": "%s",
              "supplierId": null,
              "issueDate": "2026-05-01",
              "competenceDate": "2026-05-01",
              "notes": "US5 alvo"
            }
            """.formatted(housing.getId());

        String createFoodPending = """
            {
              "description": "Supermercado",
              "originalAmount": 300.00,
              "dueDate": "2026-05-18",
              "categoryId": "%s",
              "supplierId": null,
              "issueDate": "2026-05-01",
              "competenceDate": "2026-05-01",
              "notes": "US5 outra categoria"
            }
            """.formatted(food.getId());

        String createHousingPaid = """
            {
              "description": "Condominio",
              "originalAmount": 450.00,
              "dueDate": "2026-05-20",
              "categoryId": "%s",
              "supplierId": null,
              "issueDate": "2026-05-01",
              "competenceDate": "2026-05-01",
              "notes": "US5 pago",
              "status": "PAID"
            }
            """.formatted(housing.getId());

        MvcResult targetResult = mockMvc.perform(post("/api/v1/financial-profiles/{financialProfileId}/payable-accounts", profile.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createHousingPending))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn();

        String targetId = objectMapper.readTree(targetResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/v1/financial-profiles/{financialProfileId}/payable-accounts", profile.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createFoodPending))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/financial-profiles/{financialProfileId}/payable-accounts", profile.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createHousingPaid))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(get("/api/v1/financial-profiles/{financialProfileId}/payable-accounts"
                    + "?status=PENDING&categoryId={categoryId}&dueDateFrom=2026-05-10&dueDateTo=2026-05-16",
                profile.getId(), housing.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(targetId));

        mockMvc.perform(get("/api/v1/financial-profiles/{financialProfileId}/payable-accounts", profile.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldCreateAndPreventDuplicateCategoriesPerProfile() throws Exception {
        String email = "category.user@billcore.dev";
        String password = "SenhaSegura123";

        registerUser(email, password);
        String token = loginAndGetToken(email, password);

        String createProfilePayload = """
            {
              "name": "Perfil Categorias",
              "description": "Profile para teste de categorias",
              "profileType": "PERSONAL"
            }
            """;

        MvcResult profileResult = mockMvc.perform(post("/api/v1/financial-profiles")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createProfilePayload))
            .andExpect(status().isCreated())
            .andReturn();

        String profileId = objectMapper.readTree(profileResult.getResponse().getContentAsString()).get("id").asText();

        String createCategoryPayload = """
            {
              "name": "Transporte"
            }
            """;

        mockMvc.perform(post("/api/v1/financial-profiles/{financialProfileId}/categories", profileId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCategoryPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Transporte"));

        mockMvc.perform(post("/api/v1/financial-profiles/{financialProfileId}/categories", profileId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCategoryPayload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("BUSINESS_RULE_ERROR"));
    }

    private void registerUser(String email, String password) throws Exception {
        String payload = """
            {
              "name": "RF2 User",
              "email": "%s",
              "password": "%s"
            }
            """.formatted(email, password);

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String payload = """
            {
              "email": "%s",
              "password": "%s"
            }
            """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode loginJson = objectMapper.readTree(result.getResponse().getContentAsString());
        return loginJson.get("accessToken").asText();
    }
}
