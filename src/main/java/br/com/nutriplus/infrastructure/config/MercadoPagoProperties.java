package br.com.nutriplus.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nutriplus.mercadopago")
public record MercadoPagoProperties(
        String accessToken,
        String publicKey,
        String webhookSecret,
        String frontendUrl,
        String apiPublicUrl,
        String apiBaseUrl,
        int athleteMonthlyPriceCents,
        int athleteYearlyPriceCents,
        boolean mockMode
) {
    public MercadoPagoProperties {
        if (accessToken == null) accessToken = "";
        if (publicKey == null) publicKey = "";
        if (webhookSecret == null) webhookSecret = "";
        if (frontendUrl == null || frontendUrl.isBlank()) frontendUrl = "http://localhost:4200";
        if (apiPublicUrl == null || apiPublicUrl.isBlank()) apiPublicUrl = "http://localhost:8080";
        if (apiBaseUrl == null || apiBaseUrl.isBlank()) apiBaseUrl = "https://api.mercadopago.com";
        if (athleteMonthlyPriceCents <= 0) athleteMonthlyPriceCents = 2490;
        if (athleteYearlyPriceCents <= 0) athleteYearlyPriceCents = 19900;
    }

    public boolean isConfigured() {
        return accessToken != null && !accessToken.isBlank();
    }

    public boolean isCheckoutReady() {
        return isConfigured() && publicKey != null && !publicKey.isBlank();
    }

    public boolean isMockMode() {
        return mockMode || !isConfigured();
    }
}
