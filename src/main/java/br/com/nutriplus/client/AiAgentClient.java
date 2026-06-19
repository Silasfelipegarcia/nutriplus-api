package br.com.nutriplus.client;

import br.com.nutriplus.client.dto.AiMealPlanGenerateResponse;
import br.com.nutriplus.client.dto.AiNutritionCalculateResponse;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.exception.AiAgentException;
import br.com.nutriplus.infrastructure.config.AiAgentProperties;
import br.com.nutriplus.infrastructure.web.TraceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class AiAgentClient {

    private static final Logger log = LoggerFactory.getLogger(AiAgentClient.class);

    private final AiAgentProperties aiAgentProperties;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final HttpClient httpClient;

    public AiAgentClient(AiAgentProperties aiAgentProperties, ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.aiAgentProperties = aiAgentProperties;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(aiAgentProperties.connectTimeoutSeconds()))
                .build();
    }

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
        body.put("targetWeightKg", profile.getTargetWeightKg());
        body.put("goal", profile.getGoal().name());
        body.put("activityLevel", profile.getActivityLevel().name());
        body.put("dietaryPreference", profile.getDietaryPreference().name());
        body.put("restriction", profile.getRestriction().name());
        if (profile.getAgentPersona() != null) {
            body.put("agentId", profile.getAgentPersona().toAgentId());
        }
        if (profile.getFoodLikes() != null && !profile.getFoodLikes().isBlank()) {
            body.put("foodLikes", profile.getFoodLikes());
        }
        if (profile.getFoodDislikes() != null && !profile.getFoodDislikes().isBlank()) {
            body.put("foodDislikes", profile.getFoodDislikes());
        }
        if (profile.getMealNotes() != null && !profile.getMealNotes().isBlank()) {
            body.put("mealNotes", profile.getMealNotes());
        }
        return body;
    }

    private <T> T post(String path, Object body, Class<T> responseType) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String statusTag = "success";
        int maxAttempts = aiAgentProperties.maxRetries() + 1;
        Exception lastException = null;

        try {
            String json = objectMapper.writeValueAsString(body);

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    HttpRequest.Builder builder = HttpRequest.newBuilder()
                            .uri(URI.create(aiAgentProperties.baseUrl() + path))
                            .timeout(Duration.ofSeconds(aiAgentProperties.readTimeoutSeconds()))
                            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .POST(HttpRequest.BodyPublishers.ofString(json));

                    TraceContext.currentHeaders().forEach(builder::header);

                    HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() >= 400) {
                        statusTag = "error";
                        log.error("AI agent error {}: {}", response.statusCode(), response.body());
                        throw new AiAgentException("Erro ao comunicar com agente de IA: " + response.statusCode());
                    }

                    return objectMapper.readValue(response.body(), responseType);
                } catch (AiAgentException e) {
                    throw e;
                } catch (Exception e) {
                    lastException = e;
                    if (attempt < maxAttempts) {
                        log.warn("AI agent call failed (attempt {}/{}), retrying: {}", attempt, maxAttempts, e.getMessage());
                    }
                }
            }

            statusTag = "error";
            log.error("Failed to call AI agent after {} attempts", maxAttempts, lastException);
            throw new AiAgentException("Falha na comunicação com agente de IA", lastException);
        } catch (AiAgentException e) {
            statusTag = "error";
            throw e;
        } catch (Exception e) {
            statusTag = "error";
            log.error("Failed to call AI agent", e);
            throw new AiAgentException("Falha na comunicação com agente de IA", e);
        } finally {
            sample.stop(Timer.builder("nutriplus.ai.agent.duration")
                    .tag("path", path)
                    .tag("status", statusTag)
                    .register(meterRegistry));
            meterRegistry.counter("nutriplus.ai.agent.calls", "path", path, "status", statusTag).increment();
        }
    }
}
