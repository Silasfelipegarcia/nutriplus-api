package br.com.nutriplus.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "nutriplus.ai-agent")
@Getter
@Setter
public class AiAgentProperties {
    private String baseUrl;
}
