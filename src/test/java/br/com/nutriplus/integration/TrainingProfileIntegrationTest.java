package br.com.nutriplus.integration;

import br.com.nutriplus.AbstractIntegrationTest;
import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.AiNutritionCalculateResponse;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.support.TestCpfFactory;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TrainingProfileIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiAgentClient aiAgentClient;

    @Test
    void saveTrainingProfileRecalculatesTargetCaloriesWithTrainingExtra() throws Exception {
        when(aiAgentClient.calculateMacros(any(NutritionProfile.class))).thenAnswer(macrosWithTrainingExtra());

        String email = "athlete-" + UUID.randomUUID() + "@nutriplus.test";
        String password = "secret123";
        String registerBody = """
                {"name":"Athlete User","email":"%s","password":"%s","cpf":"%s","birthDate":"1990-06-15"}
                """.formatted(email, password, TestCpfFactory.nextValidCpf());

        String authJson = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = extractJsonField(authJson, "token");
        var auth = authHeaders(token);

        String profileBody = """
                {
                  "age": 30,
                  "sex": "MALE",
                  "heightCm": 175,
                  "currentWeightKg": 80,
                  "targetWeightKg": 75,
                  "goal": "LOSE_WEIGHT",
                  "activityLevel": "MODERATE",
                  "dietaryPreference": "OMNIVORE",
                  "restriction": "NONE",
                  "agentPersona": "LUNA"
                }
                """;

        mockMvc.perform(post("/nutrition-profile")
                        .headers(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetCalories").value(1700));

        String trainingBody = """
                {
                  "athleteModeEnabled": true,
                  "activities": [
                    {"sportType": "WEIGHT_TRAINING", "daysPerWeek": 3, "minutesPerSession": 60}
                  ]
                }
                """;

        mockMvc.perform(put("/training/profile")
                        .headers(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trainingBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedToPlan").value(true))
                .andExpect(jsonPath("$.dailyExtraKcal").value(greaterThan(0)));

        mockMvc.perform(get("/nutrition-profile").headers(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.athleteModeEnabled").value(true))
                .andExpect(jsonPath("$.trainingDailyExtraKcal").value(greaterThan(0)))
                .andExpect(jsonPath("$.targetCalories").value(greaterThan(1700)));

        mockMvc.perform(put("/training/profile")
                        .headers(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"athleteModeEnabled\": false, \"activities\": []}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedToPlan").value(false));

        mockMvc.perform(get("/nutrition-profile").headers(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.athleteModeEnabled").value(false))
                .andExpect(jsonPath("$.targetCalories").value(1700));
    }

    private static Answer<AiNutritionCalculateResponse> macrosWithTrainingExtra() {
        return invocation -> {
            NutritionProfile profile = invocation.getArgument(0);
            BigDecimal extra = profile.getTrainingDailyExtraKcal() != null
                    ? profile.getTrainingDailyExtraKcal()
                    : BigDecimal.ZERO;
            BigDecimal base = new BigDecimal("1700");
            BigDecimal target = base.add(extra);
            return new AiNutritionCalculateResponse(
                    new BigDecimal("1600"),
                    new BigDecimal("2200"),
                    target,
                    new BigDecimal("130"),
                    new BigDecimal("170"),
                    new BigDecimal("55"),
                    "ESTIMATE",
                    null
            );
        };
    }

    private static org.springframework.http.HttpHeaders authHeaders(String token) {
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    private static String extractJsonField(String json, String field) {
        String needle = "\"" + field + "\":\"";
        int start = json.indexOf(needle);
        if (start < 0) {
            throw new IllegalArgumentException("Field not found: " + field);
        }
        start += needle.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }
}
