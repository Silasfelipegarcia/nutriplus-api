package br.com.nutriplus.dto.response;

import java.util.List;

public record AdminFinanceOverviewResponse(
        int year,
        int month,
        int subscriptionGrossCents,
        int subscriptionPaymentCount,
        int proPlatformFeeCents,
        int proConsultationCount,
        int totalPlatformRevenueCents,
        int activePaidSubscriptions,
        int monthlyRecurringRevenueCents,
        int projectedYearlyRevenueCents,
        List<MonthlyFinancePoint> history
) {
    public record MonthlyFinancePoint(
            int year,
            int month,
            int subscriptionGrossCents,
            int proPlatformFeeCents,
            int totalPlatformRevenueCents
    ) {
    }
}
