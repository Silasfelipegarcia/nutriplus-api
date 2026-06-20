package br.com.nutriplus.dto.response;

public record PaymentIntentResponse(
        String clientSecret,
        String paymentIntentId,
        int amountCents,
        String publishableKey,
        boolean mockMode
) {
}
