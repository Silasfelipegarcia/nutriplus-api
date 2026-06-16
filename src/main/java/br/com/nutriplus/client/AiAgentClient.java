package br.com.nutriplus.client;

import br.com.nutriplus.client.dto.AiMealPlanGenerateResponse;
import br.com.nutriplus.client.dto.AiNutritionCalculateResponse;
import br.com.nutriplus.config.AiAgentProperties;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.exception.AiAgentException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiAgentClient {

    private final AiAgentProperties aiAgentProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public AiNutritionCalculateResponse calculateMacros(NutritionProfile profile) {
        Map<String, Object> body = profileToMap(profile);
        return post("/api/v1/nutrition/calculate", body, AiNutritionCalculateResponse.class);
    }

    public AiMealPlanGenerateResponse generateMealPlan(NutritionProfile profile) {
        Map<String, Object> body = profileToMap(profile);
        body.put("targetCalories", profile.getTargetCalories());
        body.put("targetProteinG", profile.getTargetProteinG());
        body.put("targetCarbsG", profile.getTargetCarbsG());
        body.put("targetFatG", profile.getTargetFatG());
        return post("/api/v1/meal-plan/generate", body, AiMealPlanGenerateResponse.class);
    }

    private Map<String, Object> profileToMap(NutritionProfile profile) {
        Map<String, Object> body = new HashMap<>();
        body.put("age", profile.getAge());
        body.put("sex", profile.getSex().name());
        body.put("heightCm", profile.getHeightCm());
        body.put("currentWeightKg", profile.getCurrentWeightKg());
        body.put("goal", profile.getGoal().name());
        body.put("activityLevel", profile.getActivityLevel().name());
        body.put("dietaryPreference", profile.getDietaryPreference().name());
        body.put("restriction", profile.getRestriction().name());
        return body;
    }

    private <T> T post(String path, Object body, Class<T> responseType) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiAgentProperties.getBaseUrl() + path))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                log.error("AI agent error {}: {}", response.statusCode(), response.body());
                throw new AiAgentException("Erro ao comunicar com agente de IA: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), responseType);
        } catch (AiAgentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call AI agent", e);
            throw new AiAgentException("Falha na comunicação com agente de IA", e);
        }
    }
}
