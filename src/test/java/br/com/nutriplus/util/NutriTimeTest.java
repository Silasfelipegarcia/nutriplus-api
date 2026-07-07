package br.com.nutriplus.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NutriTimeTest {

    @Test
    void todayUsesBrazilTimezone() {
        assertEquals(LocalDate.now(ZoneId.of("America/Sao_Paulo")), NutriTime.today());
    }
}
