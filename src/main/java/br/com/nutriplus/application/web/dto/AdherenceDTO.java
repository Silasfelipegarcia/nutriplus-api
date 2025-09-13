package br.com.nutriplus.application.web.dto;
import jakarta.validation.constraints.*; 
public record AdherenceDTO(@NotBlank String date, boolean mealBreakfast, boolean mealLunch, boolean mealDinner, boolean workout, @NotNull Integer waterMl) { }