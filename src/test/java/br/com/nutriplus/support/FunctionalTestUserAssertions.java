package br.com.nutriplus.support;

import br.com.nutriplus.infrastructure.dev.DevDataLoader;
import br.com.nutriplus.infrastructure.dev.DevTestUserSpec;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class FunctionalTestUserAssertions {

    private FunctionalTestUserAssertions() {
    }

    public static void assertScenarioContract(MockMvc mockMvc, HttpHeaders auth, DevTestUserSpec spec)
            throws Exception {
        if (spec.adminRole()) {
            mockMvc.perform(get("/admin/access/summary").headers(auth))
                    .andExpect(status().isOk());
            return;
        }

        if (!spec.withProfile()) {
            mockMvc.perform(get("/nutrition-profile").headers(auth))
                    .andExpect(status().isNotFound());
            return;
        }

        ResultActions profile = mockMvc.perform(get("/nutrition-profile").headers(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentPersona").value(spec.agentPersona().name()))
                .andExpect(jsonPath("$.age").value(spec.age()))
                .andExpect(jsonPath("$.dietaryPreference").value(spec.dietaryPreference().name()));

        if (spec.athleteMode()) {
            profile.andExpect(jsonPath("$.athleteModeEnabled").value(true));
        }

        assertSubscription(mockMvc, auth, spec);

        if (spec.withStubPlan()) {
            mockMvc.perform(get("/meal-plans/latest").headers(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.meals").isArray())
                    .andExpect(jsonPath("$.meals", hasSize(greaterThan(0))));

            ResultActions eligibility = mockMvc.perform(get("/meal-plans/regeneration-eligibility").headers(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hasMealPlan").value(true));

            assertRegenerationEligibility(eligibility, spec);
        } else {
            mockMvc.perform(get("/meal-plans/latest").headers(auth))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/meal-plans/regeneration-eligibility").headers(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hasMealPlan").value(false))
                    .andExpect(jsonPath("$.allowedReasons", hasItem("FIRST_PLAN")));
        }
    }

    private static void assertSubscription(MockMvc mockMvc, HttpHeaders auth, DevTestUserSpec spec)
            throws Exception {
        switch (spec) {
            case PLAN_TRIAL -> mockMvc.perform(get("/nutrition-profile").headers(auth))
                    .andExpect(jsonPath("$.subscriptionStatus.emTrial").value(true))
                    .andExpect(jsonPath("$.subscriptionStatus.status").value("TRIAL"));
            case PLAN_EXPIRED -> mockMvc.perform(get("/nutrition-profile").headers(auth))
                    .andExpect(jsonPath("$.subscriptionStatus.status").value("EXPIRED"));
            case PLAN_ESSENTIAL_MONTHLY, PLAN_ESSENTIAL_YEARLY, PLAN_TEST_MONTHLY,
                 PERSONA_LUNA, PERSONA_BRUNO, AGENT_HELENA, CYCLE_REVIEW_DUE ->
                    mockMvc.perform(get("/nutrition-profile").headers(auth))
                            .andExpect(jsonPath("$.subscriptionStatus.status").value("ACTIVE"))
                            .andExpect(jsonPath("$.subscriptionStatus.plan").value(spec.subscriptionPlan().name()));
            case PLAN_ATHLETE_MONTHLY, PLAN_ATHLETE_YEARLY, ATHLETE_REGEN ->
                    mockMvc.perform(get("/nutrition-profile").headers(auth))
                            .andExpect(jsonPath("$.subscriptionStatus.status").value("ACTIVE"))
                            .andExpect(jsonPath("$.subscriptionStatus.plan").value(spec.subscriptionPlan().name()));
            default -> {
            }
        }
    }

    private static void assertRegenerationEligibility(ResultActions eligibility, DevTestUserSpec spec)
            throws Exception {
        eligibility.andExpect(jsonPath("$.planResetAvailable").value(true))
                .andExpect(jsonPath("$.allowedReasons", hasItem("PLAN_RESET")));

        switch (spec) {
            case CYCLE_LOCKED -> eligibility
                    .andExpect(jsonPath("$.oneTimeCorrectionAvailable").value(true))
                    .andExpect(jsonPath("$.daysUntilUnlock", greaterThan(0)))
                    .andExpect(jsonPath("$.allowedReasons", hasItem("ONE_TIME_CORRECTION")));
            case CYCLE_CORRECTION_USED -> eligibility
                    .andExpect(jsonPath("$.oneTimeCorrectionAvailable").value(false))
                    .andExpect(jsonPath("$.daysUntilUnlock", greaterThan(0)))
                    .andExpect(jsonPath("$.allowedReasons", not(hasItem("ONE_TIME_CORRECTION"))));
            case CYCLE_REVIEW_DUE -> eligibility
                    .andExpect(jsonPath("$.reviewDue").value(true))
                    .andExpect(jsonPath("$.daysUntilUnlock").value(0));
            case ATHLETE_REGEN -> eligibility
                    .andExpect(jsonPath("$.athleteRegenAvailable").value(true))
                    .andExpect(jsonPath("$.allowedReasons", hasItem("ATHLETE_SWITCH")));
            default -> {
            }
        }
    }

    public static String defaultPassword() {
        return DevDataLoader.TEST_PASSWORD;
    }
}
