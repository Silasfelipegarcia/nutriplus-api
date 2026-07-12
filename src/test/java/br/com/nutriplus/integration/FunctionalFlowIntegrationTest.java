package br.com.nutriplus.integration;

import br.com.nutriplus.AbstractIntegrationTest;
import br.com.nutriplus.infrastructure.dev.DevTestUserSpec;
import br.com.nutriplus.infrastructure.dev.FunctionalTestUserSeeder;
import br.com.nutriplus.support.FunctionalTestUserAssertions;
import br.com.nutriplus.support.IntegrationAuthSupport;
import org.junit.jupiter.api.Test;
import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.AiNutritionCalculateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxos funcionais ponta a ponta: onboarding, admin e treino atleta.
 */
class FunctionalFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiAgentClient aiAgentClient;

    @Autowired
    private FunctionalTestUserSeeder functionalTestUserSeeder;

    @Test
    void onboardingUserHasNoProfileUntilSaved() throws Exception {
        when(aiAgentClient.calculateMacros(any())).thenReturn(mockMacros());

        String email = DevTestUserSpec.LEGACY_TESTE2.integrationTestEmail();
        functionalTestUserSeeder.seedSpec(DevTestUserSpec.LEGACY_TESTE2, email);

        String token = IntegrationAuthSupport.loginAs(
                mockMvc, email, FunctionalTestUserAssertions.defaultPassword());
        var auth = IntegrationAuthSupport.bearerHeaders(token);

        mockMvc.perform(get("/nutrition-profile").headers(auth))
                .andExpect(status().isNotFound());

        String profileBody = """
                {
                  "age": 28,
                  "sex": "FEMALE",
                  "heightCm": 165,
                  "currentWeightKg": 68,
                  "targetWeightKg": 62,
                  "goal": "LOSE_WEIGHT",
                  "activityLevel": "LIGHT",
                  "dietaryPreference": "OMNIVORE",
                  "restriction": "NONE",
                  "agentPersona": "LUNA",
                  "foodLikes": "frango, salada",
                  "foodDislikes": "fígado"
                }
                """;

        mockMvc.perform(post("/nutrition-profile")
                        .headers(auth)
                        .contentType("application/json")
                        .content(profileBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentPersona").value("LUNA"));

        mockMvc.perform(get("/meal-plans/regeneration-eligibility").headers(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasMealPlan").value(false))
                .andExpect(jsonPath("$.allowedReasons[0]").value("FIRST_PLAN"));
    }

    @Test
    void adminUserCanAccessAdminSummary() throws Exception {
        String email = DevTestUserSpec.LEGACY_ADMIN.integrationTestEmail();
        functionalTestUserSeeder.seedSpec(DevTestUserSpec.LEGACY_ADMIN, email);

        String token = IntegrationAuthSupport.loginAs(
                mockMvc, email, FunctionalTestUserAssertions.defaultPassword());
        var auth = IntegrationAuthSupport.bearerHeaders(token);

        mockMvc.perform(get("/admin/access/summary").headers(auth))
                .andExpect(status().isOk());
    }

    @Test
    void athleteUserHasTrainingProfile() throws Exception {
        String email = DevTestUserSpec.PLAN_ATHLETE_MONTHLY.integrationTestEmail();
        functionalTestUserSeeder.seedSpec(DevTestUserSpec.PLAN_ATHLETE_MONTHLY, email);

        String token = IntegrationAuthSupport.loginAs(
                mockMvc, email, FunctionalTestUserAssertions.defaultPassword());
        var auth = IntegrationAuthSupport.bearerHeaders(token);

        mockMvc.perform(get("/training/profile").headers(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.athleteModeEnabled").value(true))
                .andExpect(jsonPath("$.activities").isArray())
                .andExpect(jsonPath("$.activities.length()").value(1));
    }

    @Test
    void helenaProfileTriggersSeniorAge() throws Exception {
        String email = DevTestUserSpec.AGENT_HELENA.integrationTestEmail();
        functionalTestUserSeeder.seedSpec(DevTestUserSpec.AGENT_HELENA, email);

        String token = IntegrationAuthSupport.loginAs(
                mockMvc, email, FunctionalTestUserAssertions.defaultPassword());
        var auth = IntegrationAuthSupport.bearerHeaders(token);

        mockMvc.perform(get("/nutrition-profile").headers(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.age").value(68))
                .andExpect(jsonPath("$.lifeStage").value("SENIOR"));
    }

    @Test
    void floraProfileIsVegetarian() throws Exception {
        String email = DevTestUserSpec.AGENT_FLORA.integrationTestEmail();
        functionalTestUserSeeder.seedSpec(DevTestUserSpec.AGENT_FLORA, email);

        String token = IntegrationAuthSupport.loginAs(
                mockMvc, email, FunctionalTestUserAssertions.defaultPassword());
        var auth = IntegrationAuthSupport.bearerHeaders(token);

        mockMvc.perform(get("/nutrition-profile").headers(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dietaryPreference").value("VEGETARIAN"));
    }

    private static AiNutritionCalculateResponse mockMacros() {
        return new AiNutritionCalculateResponse(
                new BigDecimal("1750"),
                new BigDecimal("2400"),
                new BigDecimal("1900"),
                new BigDecimal("150"),
                new BigDecimal("190"),
                new BigDecimal("63"),
                "MIFFLIN",
                null,
                null,
                null
        );
    }
}
