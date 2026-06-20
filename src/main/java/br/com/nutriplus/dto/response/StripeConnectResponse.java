package br.com.nutriplus.dto.response;

public record StripeConnectResponse(
        String onboardingUrl,
        boolean onboardingComplete,
        String stripeAccountId
) {
}
