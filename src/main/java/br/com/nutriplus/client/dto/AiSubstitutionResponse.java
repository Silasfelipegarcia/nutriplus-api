package br.com.nutriplus.client.dto;

import java.util.List;

public record AiSubstitutionResponse(
        String aiModel,
        List<AiMealItemDto> substitutions
) {
}
