package br.com.nutriplus.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nutriplus.ai-agent")
public record AiAgentProperties(
        String baseUrl,
        String internalToken,
        int connectTimeoutSeconds,
        int readTimeoutSeconds,
        int healthTimeoutSeconds,
        int maxRetries
) {
    public AiAgentProperties {
        if (connectTimeoutSeconds <= 0) {
            connectTimeoutSeconds = 10;
        }
        if (readTimeoutSeconds <= 0) {
            readTimeoutSeconds = 120;
        }
        if (healthTimeoutSeconds <= 0) {
            healthTimeoutSeconds = 3;
        }
        if (maxRetries < 0) {
            maxRetries = 0;
        }
    }
}
