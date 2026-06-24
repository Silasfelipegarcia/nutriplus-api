package br.com.nutriplus.dto.response;

import java.time.LocalDate;

public record PatientDossierResponse(
        Long patientId,
        String patientName,
        String patientEmail,
        String patientPhotoThumbnailUrl,
        LocalDate patientBirthDate,
        String cpfMasked,
        CareRelationshipResponse care,
        NutritionProfileResponse profile,
        java.util.List<BodyMeasurementResponse> measurements,
        EvolutionReportResponse evolution,
        MealPlanResponse latestMealPlan,
        ProgressReviewResponse latestProgressReview,
        CheckinStatsResponse checkinStats
) {
}
