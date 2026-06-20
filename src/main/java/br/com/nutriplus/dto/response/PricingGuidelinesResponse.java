package br.com.nutriplus.dto.response;

public record PricingGuidelinesResponse(
        int minConsultationPriceCents,
        int maxConsultationPriceCents,
        int suggestedPriceCents,
        java.math.BigDecimal platformFeePercent,
        int careDurationDaysDefault
) {
}
