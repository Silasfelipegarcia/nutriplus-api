package br.com.nutriplus.util;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TimeInputNormalizerTest {

    @Test
    void normalizePadsSingleDigitHourAndMinute() {
        assertEquals("06:00", TimeInputNormalizer.normalize("6:00"));
        assertEquals("07:05", TimeInputNormalizer.normalize("7:5"));
        assertEquals("23:00", TimeInputNormalizer.normalize("23:00"));
    }

    @Test
    void parseFlexibleReturnsLocalTime() {
        assertEquals(LocalTime.of(6, 0), TimeInputNormalizer.parseFlexible("6:00"));
        assertNull(TimeInputNormalizer.parseFlexible("  "));
    }
}
