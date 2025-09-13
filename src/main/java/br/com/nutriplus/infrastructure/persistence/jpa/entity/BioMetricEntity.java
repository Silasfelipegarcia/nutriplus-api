package br.com.nutriplus.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bio_metric")
public class BioMetricEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;
    private LocalDate reportDate;
    private Double weightKg;
    private Double bodyFatPercent;
    private Double skeletalMuscleMassKg;
    private Double phaseAngleDeg;
    @Lob
    private String notes;

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

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Double getBodyFatPercent() {
        return bodyFatPercent;
    }

    public void setBodyFatPercent(Double bodyFatPercent) {
        this.bodyFatPercent = bodyFatPercent;
    }

    public Double getSkeletalMuscleMassKg() {
        return skeletalMuscleMassKg;
    }

    public void setSkeletalMuscleMassKg(Double skeletalMuscleMassKg) {
        this.skeletalMuscleMassKg = skeletalMuscleMassKg;
    }

    public Double getPhaseAngleDeg() {
        return phaseAngleDeg;
    }

    public void setPhaseAngleDeg(Double phaseAngleDeg) {
        this.phaseAngleDeg = phaseAngleDeg;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}