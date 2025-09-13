package br.com.nutriplus.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class BioimpedanceMetric {
    private final UUID id;
    private final UUID userId;
    private final LocalDate reportDate;
    private final Double weightKg;
    private final Double bodyFatPercent;
    private final Double skeletalMuscleMassKg;
    private final Double phaseAngleDeg;
    private final String notes;

    public BioimpedanceMetric(UUID id, UUID userId, LocalDate reportDate, Double weightKg, Double bodyFatPercent, Double skeletalMuscleMassKg, Double phaseAngleDeg, String notes) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.reportDate = Objects.requireNonNull(reportDate);
        this.weightKg = weightKg;
        this.bodyFatPercent = bodyFatPercent;
        this.skeletalMuscleMassKg = skeletalMuscleMassKg;
        this.phaseAngleDeg = phaseAngleDeg;
        this.notes = notes;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public Double getBodyFatPercent() {
        return bodyFatPercent;
    }

    public Double getSkeletalMuscleMassKg() {
        return skeletalMuscleMassKg;
    }

    public Double getPhaseAngleDeg() {
        return phaseAngleDeg;
    }

    public String getNotes() {
        return notes;
    }
}
