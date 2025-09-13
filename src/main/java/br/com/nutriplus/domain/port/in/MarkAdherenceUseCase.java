package br.com.nutriplus.domain.port.in;

import java.time.LocalDate;
import java.util.UUID;

public interface MarkAdherenceUseCase {
    void mark(UUID userId, LocalDate date, boolean mealBreakfast, boolean mealLunch, boolean mealDinner, boolean workout, int waterMl);
}