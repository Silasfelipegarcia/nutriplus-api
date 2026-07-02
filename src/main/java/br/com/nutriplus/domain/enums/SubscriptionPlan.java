package br.com.nutriplus.domain.enums;

public enum SubscriptionPlan {
    FREE,
    ESSENTIAL_MONTHLY,
    ESSENTIAL_YEARLY,
    ATHLETE_MONTHLY,
    ATHLETE_YEARLY,
    /** Plano temporário de validação de cobrança em produção (R$ 1,00). */
    TEST_MONTHLY
}
