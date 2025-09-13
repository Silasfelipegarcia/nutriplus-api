package br.com.nutriplus.infrastructure.llm;
import br.com.nutriplus.domain.port.out.LLMPort; import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration;
@Configuration public class LLMConfig {
  @Bean public LLMPort llmPort(@Value("${app.llm.provider:mock}") String provider, OpenAIAdapter openAIAdapter, MockLLMAdapter mockLLMAdapter) {
    if ("openai".equalsIgnoreCase(provider)) { String test = openAIAdapter.summarizeDaily(); if (test != null) return openAIAdapter; }
    return mockLLMAdapter;
  }
}