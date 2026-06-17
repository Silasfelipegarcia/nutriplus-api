package br.com.nutriplus.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "nutriplus.observability")
public record ObservabilityProperties(
        @DefaultValue("1000") long slowRequestMs
) {
}
