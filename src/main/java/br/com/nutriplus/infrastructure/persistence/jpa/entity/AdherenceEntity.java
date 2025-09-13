package br.com.nutriplus.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "adherence", indexes = @Index(name = "idx_user_date", columnList = "userId,date", unique = true))
public class AdherenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;
    private LocalDate date;
    private Boolean mealBreakfast;
    private Boolean mealLunch;
    private Boolean mealDinner;
    private Boolean workout;
    private Integer waterMl;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Boolean getMealBreakfast() {
        return mealBreakfast;
    }

    public void setMealBreakfast(Boolean mealBreakfast) {
        this.mealBreakfast = mealBreakfast;
    }

    public Boolean getMealLunch() {
        return mealLunch;
    }

    public void setMealLunch(Boolean mealLunch) {
        this.mealLunch = mealLunch;
    }

    public Boolean getMealDinner() {
        return mealDinner;
    }

    public void setMealDinner(Boolean mealDinner) {
        this.mealDinner = mealDinner;
    }

    public Boolean getWorkout() {
        return workout;
    }

    public void setWorkout(Boolean workout) {
        this.workout = workout;
    }

    public Integer getWaterMl() {
        return waterMl;
    }

    public void setWaterMl(Integer waterMl) {
        this.waterMl = waterMl;
    }
}