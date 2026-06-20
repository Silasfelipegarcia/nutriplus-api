package br.com.nutriplus.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "idempotency")
public record IdempotencyProperties(
        boolean enabled,
        boolean requireKey,
        int ttlHours,
        int inProgressTimeoutSeconds
) {
}
