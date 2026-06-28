package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.enums.AiPlanIneligibleReason;
import br.com.nutriplus.domain.enums.PregnancyStatus;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.infrastructure.config.LegalProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HealthEligibilityServiceTest {

    private HealthEligibilityService service;

    @BeforeEach
    void setUp() {
        service = new HealthEligibilityService(new LegalProperties(
                "2026-06-2", "2026-06-2", "2026-06-2", "Termos", "Privacidade"));
    }

    @Test
    void eligibleWhenNoFlags() {
        var profile = profile(PregnancyStatus.NONE, false, false);
        var info = service.getEligibilityInfo(profile);
        assertThat(info.aiPlanEligible()).isTrue();
    }

    @Test
    void ineligibleWhenPregnant() {
        var profile = profile(PregnancyStatus.PREGNANT, false, false);
        var info = service.getEligibilityInfo(profile);
        assertThat(info.aiPlanEligible()).isFalse();
        assertThat(info.aiPlanIneligibleReason()).isEqualTo(AiPlanIneligibleReason.PREGNANCY.name());
    }

    @Test
    void multipleReasonsWhenSeveralFlags() {
        var profile = profile(PregnancyStatus.PREGNANT, true, true);
        var info = service.getEligibilityInfo(profile);
        assertThat(info.aiPlanIneligibleReason()).isEqualTo(AiPlanIneligibleReason.MULTIPLE.name());
    }

    @Test
    void assertThrowsWhenIneligible() {
        var profile = profile(PregnancyStatus.PREGNANT, false, false);
        assertThatThrownBy(() -> service.assertAiPlanAllowed(profile))
                .isInstanceOf(BusinessException.class);
    }

    private static NutritionProfile profile(PregnancyStatus status, boolean eatingDisorder, boolean renal) {
        return NutritionProfile.builder()
                .pregnancyStatus(status)
                .eatingDisorderRisk(eatingDisorder)
                .severeRenalRestriction(renal)
                .build();
    }
}
