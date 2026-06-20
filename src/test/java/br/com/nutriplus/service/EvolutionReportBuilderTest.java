package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.enums.Goal;
import br.com.nutriplus.dto.response.BodyMeasurementResponse;
import br.com.nutriplus.dto.response.EvolutionMetricResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvolutionReportBuilderTest {

    private final EvolutionReportBuilder builder = new EvolutionReportBuilder();

    @Test
    void insightsAvoidClinicalDiagnosisLanguage() {
        NutritionProfile profile = NutritionProfile.builder()
                .goal(Goal.LOSE_WEIGHT)
                .targetWeightKg(new BigDecimal("70"))
                .build();

        BodyMeasurementResponse baseline = measurement(
                1L, LocalDate.now().minusDays(15),
                "80", "25", "35", "90", "100", "95", "30", "30", "55", "55");
        BodyMeasurementResponse latest = measurement(
                2L, LocalDate.now(),
                "78", "24", "35.5", "88", "100", "95", "31", "31", "55", "55");

        List<EvolutionMetricResponse> metrics = builder.buildMetrics(profile, baseline, latest, 75);

        assertFalse(metrics.isEmpty());
        for (EvolutionMetricResponse metric : metrics) {
            String insight = metric.insight().toLowerCase();
            assertFalse(insight.contains("diagnóstico"), () -> metric.key() + ": " + metric.insight());
            assertFalse(insight.contains("redução de gordura abdominal"), () -> metric.key());
            assertFalse(insight.contains("possível ganho de massa muscular"), () -> metric.key());
        }
    }

    @Test
    void belowStatusSuggestsProfessionalWhenInDoubt() {
        NutritionProfile profile = NutritionProfile.builder()
                .goal(Goal.LOSE_WEIGHT)
                .targetWeightKg(new BigDecimal("60"))
                .build();

        BodyMeasurementResponse baseline = measurement(
                1L, LocalDate.now().minusDays(15),
                "80", "30", "32", "95", "100", "95", "28", "28", "55", "55");
        BodyMeasurementResponse latest = measurement(
                2L, LocalDate.now(),
                "81", "31", "32", "96", "100", "95", "28", "28", "55", "55");

        List<EvolutionMetricResponse> metrics = builder.buildMetrics(profile, baseline, latest, 30);
        boolean hasProfessionalHint = metrics.stream()
                .map(EvolutionMetricResponse::insight)
                .anyMatch(i -> i.toLowerCase().contains("profissional de saúde"));
        assertTrue(hasProfessionalHint);
    }

    private static BodyMeasurementResponse measurement(
            Long id,
            LocalDate date,
            String weight,
            String fat,
            String muscle,
            String waist,
            String hip,
            String chest,
            String armR,
            String armL,
            String thighR,
            String thighL
    ) {
        return new BodyMeasurementResponse(
                id,
                date,
                bd(weight),
                bd(fat),
                bd(muscle),
                bd(waist),
                bd(hip),
                bd(chest),
                null,
                bd(armR),
                bd(armL),
                bd(thighR),
                bd(thighL),
                null
        );
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
