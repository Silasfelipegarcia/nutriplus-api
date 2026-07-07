package br.com.nutriplus.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanRegenerationReasonTest {

    @Test
    void resetsTodayTrackingOnSuccess_forPlanReplacements() {
        assertFalse(PlanRegenerationReason.FIRST_PLAN.resetsTodayTrackingOnSuccess());
        assertFalse(PlanRegenerationReason.GENERATION_RETRY.resetsTodayTrackingOnSuccess());
        assertTrue(PlanRegenerationReason.ONE_TIME_CORRECTION.resetsTodayTrackingOnSuccess());
        assertTrue(PlanRegenerationReason.PLAN_RESET.resetsTodayTrackingOnSuccess());
        assertTrue(PlanRegenerationReason.UNLOCKED_REGEN.resetsTodayTrackingOnSuccess());
    }
}
