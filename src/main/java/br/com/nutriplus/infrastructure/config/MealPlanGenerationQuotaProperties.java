package br.com.nutriplus.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nutriplus.meal-plan-generation-quota")
public record MealPlanGenerationQuotaProperties(
        int limitedTierMonthlyQuota,
        int limitedTierDailyQuota,
        int unlimitedTierDailyQuota,
        int betaDailyQuota
) {
    public MealPlanGenerationQuotaProperties {
        if (limitedTierMonthlyQuota < 1) {
            limitedTierMonthlyQuota = 1;
        }
        if (limitedTierDailyQuota < 1) {
            limitedTierDailyQuota = 1;
        }
        if (unlimitedTierDailyQuota < 1) {
            unlimitedTierDailyQuota = 1;
        }
        if (betaDailyQuota < 1) {
            betaDailyQuota = 1;
        }
    }
}
