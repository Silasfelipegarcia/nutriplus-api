package br.com.nutriplus.integration;

import br.com.nutriplus.AbstractIntegrationTest;
import br.com.nutriplus.support.IntegrationAuthSupport;
import br.com.nutriplus.support.TestCpfFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AppFeedbackIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void submitAndFetchLatestFeedback() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(get("/feedback/app/latest")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        String body = """
                {
                  "easeOfUse": 4,
                  "mealPlanQuality": 5,
                  "aiHelpfulness": 4,
                  "progressTracking": 3,
                  "overallSatisfaction": 4,
                  "improvementSuggestions": "Mais opções de lanche",
                  "appVersion": "1.0.0",
                  "platform": "android"
                }
                """;

        mockMvc.perform(post("/feedback/app")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.overallSatisfaction").value(4))
                .andExpect(jsonPath("$.message").value(notNullValue()));

        mockMvc.perform(get("/feedback/app/latest")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallSatisfaction").value(4))
                .andExpect(jsonPath("$.improvementSuggestions").value("Mais opções de lanche"));
    }

    @Test
    void rejectInvalidLikertScore() throws Exception {
        String token = registerAndGetToken();

        String body = """
                {
                  "easeOfUse": 6,
                  "mealPlanQuality": 4,
                  "aiHelpfulness": 4,
                  "progressTracking": 3,
                  "overallSatisfaction": 4
                }
                """;

        mockMvc.perform(post("/feedback/app")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectUnauthenticatedFeedback() throws Exception {
        mockMvc.perform(post("/feedback/app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "easeOfUse": 4,
                                  "mealPlanQuality": 4,
                                  "aiHelpfulness": 4,
                                  "progressTracking": 4,
                                  "overallSatisfaction": 4
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    private String registerAndGetToken() throws Exception {
        String email = "feedback-" + UUID.randomUUID() + "@nutriplus.test";
        return IntegrationAuthSupport.registerAndLogin(
                mockMvc,
                userRepository,
                "Feedback User",
                email,
                "secret123",
                TestCpfFactory.nextValidCpf());
    }
}
