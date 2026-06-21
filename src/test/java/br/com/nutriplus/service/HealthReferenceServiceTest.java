package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.enums.Goal;
import br.com.nutriplus.domain.enums.Sex;
import br.com.nutriplus.dto.response.BodyMeasurementResponse;
import br.com.nutriplus.dto.response.HealthIndicatorResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthReferenceServiceTest {

    private final HealthReferenceService service = new HealthReferenceService();

    @Test
    void calculateBmiAtNormalRange() {
        BigDecimal bmi = service.calculateBmi(new BigDecimal("70"), new BigDecimal("175"));
        assertNotNull(bmi);
        assertEquals(new BigDecimal("22.9"), bmi);
    }

    @Test
    void bmiClassificationAtBoundaries() {
        NutritionProfile profile = profile(Sex.MALE, Goal.LOSE_WEIGHT, false, 30);
        profile.setHeightCm(new BigDecimal("170"));

        assertEquals("Normal", bmiClass(profile, "64.6")); // ~22.4
        assertEquals("Sobrepeso", bmiClass(profile, "73")); // ~25.3
        assertEquals("Obesidade grau I", bmiClass(profile, "87")); // ~30.1
        assertEquals("Baixo peso", bmiClass(profile, "50")); // ~17.3
    }

    @Test
    void waistClassificationBySex() {
        var indicators = service.buildHealthSnapshot(
                profile(Sex.MALE, Goal.LOSE_WEIGHT, false, 35),
                measurement("80", null, "90", "100"),
                measurement("78", null, "95", "100")
        );
        HealthIndicatorResponse waist = find(indicators, "waist");
        assertNotNull(waist);
        assertEquals("Risco aumentado", waist.classification());
        assertEquals("ATTENTION", waist.riskLevel());

        var female = service.buildHealthSnapshot(
                profile(Sex.FEMALE, Goal.LOSE_WEIGHT, false, 35),
                measurement("65", null, "75", "100"),
                measurement("64", null, "82", "100")
        );
        HealthIndicatorResponse femaleWaist = find(female, "waist");
        assertNotNull(femaleWaist);
        assertEquals("Risco aumentado", femaleWaist.classification());
    }

    @Test
    void waistHipRatioHighRiskForMale() {
        var indicators = service.buildHealthSnapshot(
                profile(Sex.MALE, Goal.LOSE_WEIGHT, false, 40),
                measurement("85", null, "90", "100"),
                measurement("84", null, "95", "100")
        );
        HealthIndicatorResponse ratio = find(indicators, "waistHipRatio");
        assertNotNull(ratio);
        assertEquals(new BigDecimal("0.95"), ratio.value());
        assertEquals("HIGH_RISK", ratio.riskLevel());
    }

    @Test
    void deltaImprovedWhenBmiDropsForWeightLossGoal() {
        NutritionProfile profile = profile(Sex.MALE, Goal.LOSE_WEIGHT, false, 35);
        var indicators = service.buildHealthSnapshot(
                profile,
                measurement("85", "25", "90", "100"),
                measurement("80", "23", "88", "100")
        );
        HealthIndicatorResponse bmi = find(indicators, "bmi");
        assertNotNull(bmi);
        assertEquals("IMPROVED", bmi.deltaDirection());
        assertTrue(bmi.deltaSinceBaseline().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    void athleteModeNoteOnBmi() {
        NutritionProfile profile = profile(Sex.MALE, Goal.GAIN_MASS, true, 28);
        var indicators = service.buildHealthSnapshot(
                profile,
                measurement("90", "15", "85", "100"),
                measurement("92", "14", "84", "100")
        );
        HealthIndicatorResponse bmi = find(indicators, "bmi");
        assertNotNull(bmi);
        assertTrue(bmi.healthNote().toLowerCase().contains("musculosas"));
    }

    @Test
    void seniorModeNoteOnBmi() {
        NutritionProfile profile = profile(Sex.FEMALE, Goal.MAINTAIN_WEIGHT, false, 70);
        var indicators = service.buildHealthSnapshot(
                profile,
                measurement("68", "28", "85", "100"),
                measurement("68", "28", "85", "100")
        );
        HealthIndicatorResponse bmi = find(indicators, "bmi");
        assertNotNull(bmi);
        assertTrue(bmi.healthNote().toLowerCase().contains("idosos"));
    }

    @Test
    void bodyFatOmittedWhenNull() {
        var indicators = service.buildHealthSnapshot(
                profile(Sex.MALE, Goal.LOSE_WEIGHT, false, 30),
                measurement("80", null, "90", "100"),
                measurement("78", null, "88", "100")
        );
        assertNull(find(indicators, "bodyFat"));
        assertNotNull(find(indicators, "bmi"));
    }

    @Test
    void healthNotesAvoidDiagnosisLanguage() {
        var indicators = service.buildHealthSnapshot(
                profile(Sex.MALE, Goal.LOSE_WEIGHT, false, 35),
                measurement("90", "28", "100", "105"),
                measurement("88", "27", "98", "104")
        );
        for (HealthIndicatorResponse indicator : indicators) {
            assertFalse(indicator.healthNote().toLowerCase().contains("diagnóstico clínico"));
            assertTrue(indicator.healthNote().toLowerCase().contains("não é diagnóstico")
                    || indicator.key().equals("waist")
                    || indicator.key().equals("waistHipRatio")
                    || indicator.key().equals("bodyFat"));
        }
    }

    @Test
    void buildBmiSnapshotFromProfile() {
        NutritionProfile profile = profile(Sex.MALE, Goal.LOSE_WEIGHT, false, 30);
        profile.setCurrentWeightKg(new BigDecimal("80"));
        profile.setHeightCm(new BigDecimal("175"));
        HealthIndicatorResponse snapshot = service.buildBmiSnapshot(profile);
        assertNotNull(snapshot);
        assertEquals("bmi", snapshot.key());
        assertEquals("Sobrepeso", snapshot.classification());
    }

    private String bmiClass(NutritionProfile profile, String weight) {
        var indicators = service.buildHealthSnapshot(
                profile,
                measurement(weight, null, null, null),
                measurement(weight, null, null, null)
        );
        return find(indicators, "bmi").classification();
    }

    private static HealthIndicatorResponse find(java.util.List<HealthIndicatorResponse> list, String key) {
        return list.stream().filter(i -> key.equals(i.key())).findFirst().orElse(null);
    }

    private static NutritionProfile profile(Sex sex, Goal goal, boolean athlete, int age) {
        NutritionProfile p = NutritionProfile.builder()
                .sex(sex)
                .goal(goal)
                .age(age)
                .athleteModeEnabled(athlete)
                .heightCm(new BigDecimal("170"))
                .currentWeightKg(new BigDecimal("75"))
                .targetWeightKg(new BigDecimal("70"))
                .build();
        p.setCreatedAt(LocalDateTime.now());
        return p;
    }

    private static BodyMeasurementResponse measurement(String weight, String fat, String waist, String hip) {
        return new BodyMeasurementResponse(
                1L,
                LocalDate.now(),
                weight != null ? new BigDecimal(weight) : null,
                fat != null ? new BigDecimal(fat) : null,
                null,
                waist != null ? new BigDecimal(waist) : null,
                hip != null ? new BigDecimal(hip) : null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
