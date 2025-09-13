package br.com.nutriplus.infrastructure.llm;

import br.com.nutriplus.domain.port.out.LLMPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class OpenAIAdapter implements LLMPort {
    private final WebClient webClient;
    private final String model;
    private final String apiKey;

    public OpenAIAdapter(@Value("${app.llm.openai.apiKey:}") String apiKey, @Value("${app.llm.openai.model:gpt-4o-mini}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.webClient = WebClient.builder().baseUrl("https://api.openai.com/v1/chat/completions").defaultHeader("Authorization", "Bearer " + apiKey).build();
    }

    @Override
    public String summarizeDaily() {
        if (apiKey == null || apiKey.isBlank()) return null;
        try {
            Map<String, Object> body = Map.of("model", model, "messages", new Object[]{Map.of("role", "user", "content", "Gere um resumo curto e motivador para o dia de treino e alimentação (pt-BR).")}, "temperature", 0.3);
            Map<?, ?> response = webClient.post().contentType(MediaType.APPLICATION_JSON).bodyValue(body).retrieve().bodyToMono(Map.class).onErrorResume(e -> Mono.empty()).block();
            if (response == null) return null;
            Object choices = response.get("choices");
            if (choices instanceof java.util.List<?> list && !list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof java.util.Map<?, ?> choice) {
                    Object message = choice.get("message");
                    if (message instanceof java.util.Map<?, ?> m) {
                        Object content = m.get("content");
                        if (content != null) return content.toString();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}