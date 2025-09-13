package br.com.nutriplus.domain.port.in;

import java.util.UUID;

public interface GeneratePlanUseCase {
    int generate(UUID userId);
}