package br.com.nutriplus.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        boolean enabled,
        int authRequestsPerWindow,
        int generalRequestsPerWindow,
        long windowSeconds
) {
}
