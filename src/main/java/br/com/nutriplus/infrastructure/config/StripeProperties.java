package br.com.nutriplus.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nutriplus.stripe")
public record StripeProperties(
        String secretKey,
        String webhookSecret,
        String connectReturnUrl,
        String connectRefreshUrl,
        boolean mockMode
) {
    public boolean isConfigured() {
        return secretKey != null && !secretKey.isBlank();
    }
}
