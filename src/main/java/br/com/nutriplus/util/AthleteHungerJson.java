package br.com.nutriplus.util;

import br.com.nutriplus.domain.enums.AthleteMealHunger;
import br.com.nutriplus.dto.request.AthleteHungerByMealRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class AthleteHungerJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AthleteHungerJson() {
    }

    public static String toJson(AthleteHungerByMealRequest hunger) {
        if (hunger == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(hunger);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Falha ao serializar fome por refeição", e);
        }
    }

    public static AthleteHungerByMealRequest fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(json);
            return new AthleteHungerByMealRequest(
                    parseLevel(node, "breakfast"),
                    parseLevel(node, "lunch"),
                    parseLevel(node, "afternoonSnack"),
                    parseLevel(node, "dinner")
            );
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static AthleteMealHunger parseLevel(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        try {
            return AthleteMealHunger.valueOf(node.get(field).asText());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
