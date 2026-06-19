package br.com.nutriplus.infrastructure.health;

import br.com.nutriplus.infrastructure.config.AiAgentProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component("aiAgent")
public class AiAgentHealthIndicator implements HealthIndicator {

    private final AiAgentProperties aiAgentProperties;
    private final HttpClient httpClient;

    public AiAgentHealthIndicator(AiAgentProperties aiAgentProperties) {
        this.aiAgentProperties = aiAgentProperties;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(aiAgentProperties.connectTimeoutSeconds()))
                .build();
    }

    @Override
    public Health health() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiAgentProperties.baseUrl() + "/health"))
                    .timeout(Duration.ofSeconds(aiAgentProperties.healthTimeoutSeconds()))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Health.up().withDetail("url", aiAgentProperties.baseUrl()).build();
            }
            return Health.down().withDetail("status", response.statusCode()).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
