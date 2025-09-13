package br.com.nutriplus.infrastructure.llm;

import br.com.nutriplus.domain.port.out.LLMPort;
import org.springframework.stereotype.Component;

@Component
public class MockLLMAdapter implements LLMPort {
    @Override
    public String summarizeDaily() {
        return "Foco do dia: proteína em todas as refeições, 2L de água e treino de 40–50 minutos. (mock)";
    }
}