package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.NutritionProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HydrationTargetServiceTest {

    private HydrationTargetService service;

    @BeforeEach
    void setUp() {
        service = new HydrationTargetService();
    }

    @Test
    void computesBaseTargetFromWeight() {
        NutritionProfile profile = profileWithWeight(70);
        assertEquals(2450, service.computeDailyWaterTargetMl(profile));
    }

    @Test
    void addsAthleteBonusWhenTrainingExtraPresent() {
        NutritionProfile profile = profileWithWeight(70);
        profile.setAthleteModeEnabled(true);
        profile.setTrainingDailyExtraKcal(BigDecimal.valueOf(350));
        assertEquals(2950, service.computeDailyWaterTargetMl(profile));
    }

    @Test
    void returnsNullForSevereRenalRestriction() {
        NutritionProfile profile = profileWithWeight(70);
        profile.setSevereRenalRestriction(true);
        assertNull(service.computeDailyWaterTargetMl(profile));
    }

    @Test
    void returnsNullWithoutWeight() {
        NutritionProfile profile = NutritionProfile.builder().build();
        assertNull(service.computeDailyWaterTargetMl(profile));
    }

    @Test
    void capsAtMaxMl() {
        NutritionProfile profile = profileWithWeight(120);
        profile.setAthleteModeEnabled(true);
        profile.setTrainingDailyExtraKcal(BigDecimal.valueOf(500));
        assertEquals(4000, service.computeDailyWaterTargetMl(profile));
    }

    @Test
    void syncHydrationTargetPersistsOnProfile() {
        NutritionProfile profile = profileWithWeight(72);
        service.syncHydrationTarget(profile);
        assertEquals(2500, profile.getDailyWaterTargetMl());
    }

    private static NutritionProfile profileWithWeight(double kg) {
        return NutritionProfile.builder()
                .currentWeightKg(BigDecimal.valueOf(kg))
                .build();
    }
}
