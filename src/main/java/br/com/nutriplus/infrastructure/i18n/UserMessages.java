package br.com.nutriplus.infrastructure.i18n;

/**
 * Mensagens user-facing centralizadas (PT-BR). Preferir templates curtos.
 */
public final class UserMessages {

    private UserMessages() {
    }

    public static final String ADMIN_FORBIDDEN = "Acesso restrito a administradores.";
    public static final String ACCESS_DENIED = "Acesso negado.";
    public static final String NOT_AUTHENTICATED = "Não autenticado.";

    public static final String AI_PLAN_INELIGIBLE_BASE =
            "Planos por IA não estão disponíveis para o seu perfil.";
    public static final String AI_PLAN_INELIGIBLE_CTA = "Consulte um nutricionista pelo NutriPlus.";

    public static String aiPlanIneligible(AiPlanIneligibleCode code) {
        return switch (code) {
            case PREGNANCY -> AI_PLAN_INELIGIBLE_BASE + " (gravidez). " + AI_PLAN_INELIGIBLE_CTA;
            case BREASTFEEDING -> AI_PLAN_INELIGIBLE_BASE + " (amamentação). " + AI_PLAN_INELIGIBLE_CTA;
            case EATING_DISORDER -> AI_PLAN_INELIGIBLE_BASE + " (transtorno alimentar). " + AI_PLAN_INELIGIBLE_CTA;
            case SEVERE_RENAL -> AI_PLAN_INELIGIBLE_BASE + " (restrição renal grave). " + AI_PLAN_INELIGIBLE_CTA;
            case MULTIPLE -> AI_PLAN_INELIGIBLE_BASE + " " + AI_PLAN_INELIGIBLE_CTA;
        };
    }

    public static String dailyQuotaExceeded(int dailyLimit) {
        return "Limite de " + dailyLimit + " gerações por dia atingido. Tente amanhã.";
    }

    public static String dailyQuotaExceededUpgrade() {
        return "Você já gerou seu plano de hoje. Tente amanhã ou faça upgrade para o plano Atleta.";
    }

    public static String monthlyQuotaFree() {
        return "Limite de 1 plano por mês no gratuito. Assine o Essencial ou inicie o trial.";
    }

    public static String monthlyQuotaEssential() {
        return "Limite mensal atingido no Essencial. Faça upgrade para Atleta.";
    }

    public enum AiPlanIneligibleCode {
        PREGNANCY,
        BREASTFEEDING,
        EATING_DISORDER,
        SEVERE_RENAL,
        MULTIPLE
    }
}
