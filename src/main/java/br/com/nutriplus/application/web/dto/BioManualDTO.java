package br.com.nutriplus.application.web.dto;
import jakarta.validation.constraints.*; 
public record BioManualDTO(@NotBlank String reportDate, @NotNull Double weightKg, Double bodyFatPercent, Double skeletalMuscleMassKg, Double phaseAngleDeg, String notes) { }