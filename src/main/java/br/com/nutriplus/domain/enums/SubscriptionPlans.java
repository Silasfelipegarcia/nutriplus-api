package br.com.nutriplus.domain.enums;

import java.util.EnumSet;
import java.util.Set;

/** Regras de tier B2C (Essencial vs Atleta). */
public final class SubscriptionPlans {

    private static final Set<SubscriptionPlan> ESSENTIAL = EnumSet.of(
            SubscriptionPlan.ESSENTIAL_MONTHLY,
            SubscriptionPlan.ESSENTIAL_YEARLY
    );

    private static final Set<SubscriptionPlan> ATHLETE = EnumSet.of(
            SubscriptionPlan.ATHLETE_MONTHLY,
            SubscriptionPlan.ATHLETE_YEARLY
    );

    private static final Set<SubscriptionPlan> PAID_B2C = EnumSet.of(
            SubscriptionPlan.ESSENTIAL_MONTHLY,
            SubscriptionPlan.ESSENTIAL_YEARLY,
            SubscriptionPlan.ATHLETE_MONTHLY,
            SubscriptionPlan.ATHLETE_YEARLY
    );

    private SubscriptionPlans() {
    }

    public static boolean isEssentialPlan(SubscriptionPlan plan) {
        return plan != null && ESSENTIAL.contains(plan);
    }

    public static boolean isAthletePlan(SubscriptionPlan plan) {
        return plan != null && ATHLETE.contains(plan);
    }

    public static boolean isPaidB2cPlan(SubscriptionPlan plan) {
        return plan != null && PAID_B2C.contains(plan);
    }

    public static boolean isMonthlyPlan(SubscriptionPlan plan) {
        return plan == SubscriptionPlan.ESSENTIAL_MONTHLY || plan == SubscriptionPlan.ATHLETE_MONTHLY;
    }

    public static boolean isYearlyPlan(SubscriptionPlan plan) {
        return plan == SubscriptionPlan.ESSENTIAL_YEARLY || plan == SubscriptionPlan.ATHLETE_YEARLY;
    }

    /** Plano anual equivalente (Essencial↔Essencial, Atleta↔Atleta). */
    public static SubscriptionPlan yearlyCounterpart(SubscriptionPlan plan) {
        return switch (plan) {
            case ESSENTIAL_MONTHLY -> SubscriptionPlan.ESSENTIAL_YEARLY;
            case ATHLETE_MONTHLY -> SubscriptionPlan.ATHLETE_YEARLY;
            default -> plan;
        };
    }

    /** Upgrade de tier dentro do mesmo período (Essencial→Atleta mensal/anual). */
    public static SubscriptionPlan athleteCounterpart(SubscriptionPlan essentialPlan) {
        return switch (essentialPlan) {
            case ESSENTIAL_MONTHLY -> SubscriptionPlan.ATHLETE_MONTHLY;
            case ESSENTIAL_YEARLY -> SubscriptionPlan.ATHLETE_YEARLY;
            default -> essentialPlan;
        };
    }
}
