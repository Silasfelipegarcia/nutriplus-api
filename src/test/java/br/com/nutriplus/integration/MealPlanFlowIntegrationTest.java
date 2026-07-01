package br.com.nutriplus.integration;

import br.com.nutriplus.AbstractIntegrationTest;
import br.com.nutriplus.support.TestCpfFactory;
import br.com.nutriplus.support.TestRegisterFactory;
import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.AiMealDto;
import br.com.nutriplus.client.dto.AiMealItemDto;
import br.com.nutriplus.client.dto.AiMealPlanGenerateResponse;
import br.com.nutriplus.client.dto.AiNutritionCalculateResponse;
import br.com.nutriplus.client.dto.AiShoppingItemDto;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.service.MealPlanGenerationProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MealPlanFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MealPlanGenerationProcessor generationProcessor;

    @MockBean
    private AiAgentClient aiAgentClient;

    @Test
    void generateMealPlanThenLatestReturns200() throws Exception {
        when(aiAgentClient.calculateMacros(any(NutritionProfile.class))).thenReturn(mockMacros());
        when(aiAgentClient.generateMealPlan(any(NutritionProfile.class))).thenReturn(mockMealPlan());

        String email = "mealplan-" + UUID.randomUUID() + "@nutriplus.test";
        String password = "secret123";
        String registerBody = TestRegisterFactory.body("Meal Plan User", email, password, TestCpfFactory.nextValidCpf());

        String authJson = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = extractJsonField(authJson, "token");
        long userId = Long.parseLong(extractJsonNumberField(authJson, "user", "id"));
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
                  "agentPersona": "LUNA",
                  "foodLikes": "frango, arroz",
                  "foodDislikes": "fígado"
                }
                """;

        mockMvc.perform(post("/nutrition-profile")
                        .headers(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/meal-plans/latest").headers(auth))
                .andExpect(status().isNotFound());

        String generateJson = mockMvc.perform(post("/meal-plans/generate").headers(auth))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.jobId").value(notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long jobId = Long.parseLong(extractJsonNumberField(generateJson, null, "jobId"));
        generationProcessor.run(jobId, userId, System.currentTimeMillis());

        mockMvc.perform(get("/meal-plans/generation-status").headers(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.mealPlanId").value(notNullValue()));

        mockMvc.perform(get("/meal-plans/latest").headers(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meals").isArray())
                .andExpect(jsonPath("$.totalCalories").value(1900));

        mockMvc.perform(get("/shopping-list/latest").headers(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    private static org.springframework.http.HttpHeaders authHeaders(String token) {
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    private static AiNutritionCalculateResponse mockMacros() {
        return new AiNutritionCalculateResponse(
                new BigDecimal("1750"),
                new BigDecimal("2400"),
                new BigDecimal("1900"),
                new BigDecimal("150"),
                new BigDecimal("190"),
                new BigDecimal("63"),
                "ESTIMATE",
                null
        );
    }

    private static AiMealPlanGenerateResponse mockMealPlan() {
        return new AiMealPlanGenerateResponse(
                "mock-integration",
                new BigDecimal("1900"),
                new BigDecimal("150"),
                new BigDecimal("190"),
                new BigDecimal("63"),
                List.of(new AiMealDto(
                        "BREAKFAST",
                        "Café da manhã",
                        1,
                        "07:30",
                        List.of(new AiMealItemDto(
                                "Ovos mexidos",
                                new BigDecimal("120"),
                                "2 ovos",
                                "unit",
                                new BigDecimal("186"),
                                new BigDecimal("16"),
                                new BigDecimal("2"),
                                new BigDecimal("12")
                        ))
                )),
                List.of(new AiShoppingItemDto(
                        "Ovos",
                        "12 unidades",
                        "Proteínas",
                        "ANIMAL",
                        "LEAN",
                        840,
                        "Fonte de proteína",
                        List.of("Clara de ovo"),
                        null,
                        null,
                        null,
                        null,
                        null
                )),
                null,
                "APPROVED",
                null,
                null,
                null,
                null,
                null
        );
    }

    private static String extractJsonField(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalStateException("Field not found: " + field);
        }
        start += marker.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }

    private static String extractJsonNumberField(String json, String objectName, String field) {
        String search = json;
        if (objectName != null) {
            String objectMarker = "\"" + objectName + "\":{";
            int objectStart = json.indexOf(objectMarker);
            if (objectStart < 0) {
                throw new IllegalStateException("Object not found: " + objectName);
            }
            int braceStart = objectStart + objectMarker.length() - 1;
            int depth = 0;
            int objectEnd = braceStart;
            for (int i = braceStart; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        objectEnd = i + 1;
                        break;
                    }
                }
            }
            search = json.substring(objectStart, objectEnd);
        }

        String marker = "\"" + field + "\":";
        int start = search.indexOf(marker);
        if (start < 0) {
            throw new IllegalStateException("Field not found: " + field);
        }
        start += marker.length();
        int end = start;
        while (end < search.length() && (Character.isDigit(search.charAt(end)) || search.charAt(end) == '.')) {
            end++;
        }
        return search.substring(start, end);
    }
}
