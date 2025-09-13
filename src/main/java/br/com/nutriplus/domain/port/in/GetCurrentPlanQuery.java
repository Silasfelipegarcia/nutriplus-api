package br.com.nutriplus.domain.port.in;

import br.com.nutriplus.domain.model.Plan;

import java.util.UUID;

public interface GetCurrentPlanQuery {
    Plan get(UUID userId);
}