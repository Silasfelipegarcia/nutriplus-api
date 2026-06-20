package br.com.nutriplus.dto.response;

import java.util.List;

public record PatientDossierResponse(
        Long patientId,
        String patientName,
        CareRelationshipResponse care,
        NutritionProfileResponse profile,
        List<BodyMeasurementResponse> measurements,
        EvolutionReportResponse evolution,
        MealPlanResponse latestMealPlan,
        ProgressReviewResponse latestProgressReview,
        CheckinStatsResponse checkinStats
) {
}
