package br.com.nutriplus.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        boolean enabled,
        int authRequestsPerWindow,
        int generalRequestsPerWindow,
        long windowSeconds,
        int mealPlanGeneratePerHour,
        int nutritionProfilePerHour,
        List<String> trustedProxyIps
) {
    public RateLimitProperties {
        if (trustedProxyIps == null) {
            trustedProxyIps = List.of("127.0.0.1", "::1");
        }
    }
}
