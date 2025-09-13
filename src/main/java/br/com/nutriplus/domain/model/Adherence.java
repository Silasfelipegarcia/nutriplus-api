package br.com.nutriplus.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Adherence {
    private final UUID id;
    private final UUID userId;
    private final LocalDate date;
    private final boolean mealBreakfast;
    private final boolean mealLunch;
    private final boolean mealDinner;
    private final boolean workout;
    private final Integer waterMl;

    public Adherence(UUID id, UUID userId, LocalDate date, boolean mealBreakfast, boolean mealLunch, boolean mealDinner, boolean workout, Integer waterMl) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.date = Objects.requireNonNull(date);
        this.mealBreakfast = mealBreakfast;
        this.mealLunch = mealLunch;
        this.mealDinner = mealDinner;
        this.workout = workout;
        this.waterMl = waterMl;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isMealBreakfast() {
        return mealBreakfast;
    }

    public boolean isMealLunch() {
        return mealLunch;
    }

    public boolean isMealDinner() {
        return mealDinner;
    }

    public boolean isWorkout() {
        return workout;
    }

    public Integer getWaterMl() {
        return waterMl;
    }
}
