package br.com.nutriplus.service;

import br.com.nutriplus.client.AiAgentClient;
import br.com.nutriplus.client.dto.AiTrainingConsultResponse;
import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.UserTrainingActivity;
import br.com.nutriplus.dto.response.CoachInsightResponse;
import br.com.nutriplus.dto.response.DailyFoodExtraResponse;
import br.com.nutriplus.dto.response.TodayCheckinsResponse;
import br.com.nutriplus.dto.response.TrainingActivityResponse;
import br.com.nutriplus.dto.response.TrainingProfileResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CoachInsightService {

    private final AiAgentClient aiAgentClient;

    public CoachInsightService(AiAgentClient aiAgentClient) {
        this.aiAgentClient = aiAgentClient;
    }

    public CoachInsightResponse trainingReview(
            NutritionProfile profile,
            TrainingProfileResponse trainingProfile) {
        if (!profile.isAthleteModeEnabled() || trainingProfile.activities().isEmpty()) {
            return null;
        }
        Map<String, Object> body = baseBody(profile, trainingProfile);
        body.put("intent", "VALIDATE_TRAINING");
        return mapResponse(aiAgentClient.consultTrainingGarcia(body));
    }

    public CoachInsightResponse balanceInsight(
            NutritionProfile profile,
            TrainingProfileResponse trainingProfile,
            TodayCheckinsResponse today) {
        Map<String, Object> body = baseBody(profile, trainingProfile);
        body.put("intent", "EXPLAIN_DAILY_BALANCE");
        body.put("consumedCalories", today.consumedCalories());
        body.put("extraCalories", today.extraCalories());
        body.put("totalIntakeCalories", today.totalIntakeCalories());
        List<Map<String, Object>> extras = today.extras().stream()
                .map(this::extraToMap)
                .toList();
        body.put("extras", extras);
        return mapResponse(aiAgentClient.consultTrainingGarcia(body));
    }

    private Map<String, Object> baseBody(NutritionProfile profile, TrainingProfileResponse trainingProfile) {
        Map<String, Object> body = new HashMap<>();
        body.put("agentId", profile.getAgentPersona().toAgentId());
        body.put("goal", profile.getGoal().name());
        if (profile.getCurrentWeightKg() != null) {
            body.put("currentWeightKg", profile.getCurrentWeightKg());
        }
        body.put("athleteModeEnabled", profile.isAthleteModeEnabled());
        body.put("weeklyTrainingKcal", trainingProfile.weeklyTrainingKcal());
        body.put("dailyTrainingExtraKcal", trainingProfile.dailyExtraKcal());
        body.put("trainingAppliedToPlan", trainingProfile.appliedToPlan());
        if (profile.getTargetCalories() != null) {
            body.put("targetCalories", profile.getTargetCalories().intValue());
        }
        if (trainingProfile.baseTargetCalories() != null) {
            body.put("baseTargetCalories", trainingProfile.baseTargetCalories().intValue());
        }
        List<Map<String, Object>> activityMaps = trainingProfile.activities().stream()
                .map(this::activityToMap)
                .toList();
        body.put("activities", activityMaps);
        return body;
    }

    private Map<String, Object> activityToMap(TrainingActivityResponse a) {
        Map<String, Object> m = new HashMap<>();
        m.put("sportType", a.sportType());
        m.put("label", a.label());
        m.put("daysPerWeek", a.daysPerWeek());
        m.put("minutesPerSession", a.minutesPerSession());
        m.put("caloriesPerSession", a.caloriesPerSession());
        m.put("caloriesPerWeek", a.caloriesPerWeek());
        return m;
    }

    private Map<String, Object> extraToMap(DailyFoodExtraResponse extra) {
        Map<String, Object> map = new HashMap<>();
        map.put("description", extra.description());
        map.put("estimatedCalories", extra.estimatedCalories());
        return map;
    }

    private CoachInsightResponse mapResponse(AiTrainingConsultResponse ai) {
        return new CoachInsightResponse(
                ai.summary(),
                ai.details() != null ? ai.details() : List.of(),
                ai.warnings() != null ? ai.warnings() : List.of(),
                ai.suggestedActions() != null ? ai.suggestedActions() : List.of()
        );
    }
}
