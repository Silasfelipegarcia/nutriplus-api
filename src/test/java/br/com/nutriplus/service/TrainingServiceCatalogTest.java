package br.com.nutriplus.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingServiceCatalogTest {

    @Test
    void sportCatalogIncludesExpandedPractices() {
        TrainingService service = new TrainingService(null, null, null, null, null, null, null, null, null);
        var catalog = service.getSportCatalog();

        assertFalse(catalog.isEmpty());
        assertTrue(catalog.stream().anyMatch(s -> "TENNIS".equals(s.sportType())));
        assertTrue(catalog.stream().anyMatch(s -> "FUNCTIONAL".equals(s.sportType())));
        assertTrue(catalog.stream().anyMatch(s -> "BEACH_TENNIS".equals(s.sportType())));
        assertTrue(catalog.stream().anyMatch(s -> "ROWING".equals(s.sportType())));
    }
}
