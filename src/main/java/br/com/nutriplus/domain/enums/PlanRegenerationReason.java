package br.com.nutriplus.domain.enums;

public enum PlanRegenerationReason {
    FIRST_PLAN,
    ATHLETE_SWITCH,
    ONE_TIME_CORRECTION,
    CYCLE_REVIEW,
    GENERATION_RETRY,
    NUTRITIONIST_BYPASS,
    UNLOCKED_REGEN,
    PLAN_RESET,
    HOUSEHOLD_SHARED_PLAN;

    /** Novo plano substitui o anterior — check-ins do dia devem zerar. */
    public boolean resetsTodayTrackingOnSuccess() {
        return this != FIRST_PLAN && this != GENERATION_RETRY;
    }
}
