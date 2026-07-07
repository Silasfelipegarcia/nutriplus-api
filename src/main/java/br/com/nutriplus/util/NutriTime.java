package br.com.nutriplus.util;

import java.time.LocalDate;
import java.time.ZoneId;

public final class NutriTime {

    public static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private NutriTime() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }
}
