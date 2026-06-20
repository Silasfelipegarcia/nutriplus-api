package br.com.nutriplus.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SportTypeTest {

    @Test
    void expandedSportsHavePortugueseLabels() {
        assertEquals("Tênis", SportType.TENNIS.labelPt());
        assertEquals("Treino funcional", SportType.FUNCTIONAL.labelPt());
        assertEquals("Beach tennis", SportType.BEACH_TENNIS.labelPt());
        assertEquals("Alongamento", SportType.STRETCHING.labelPt());
    }

    @Test
    void catalogIncludesAtLeastTwentyThreeSports() {
        assertTrue(SportType.values().length >= 23);
    }

    @Test
    void otherUsesDefaultMet() {
        assertEquals(5.5, SportType.OTHER.met());
    }
}
