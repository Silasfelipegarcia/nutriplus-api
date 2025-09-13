package br.com.nutriplus.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "goal")
public class GoalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;
    private String type;
    private String pace;
    private Double targetWeightKg;
    private Double targetBfPercent;
    private OffsetDateTime createdAt;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPace() {
        return pace;
    }

    public void setPace(String pace) {
        this.pace = pace;
    }

    public Double getTargetWeightKg() {
        return targetWeightKg;
    }

    public void setTargetWeightKg(Double targetWeightKg) {
        this.targetWeightKg = targetWeightKg;
    }

    public Double getTargetBfPercent() {
        return targetBfPercent;
    }

    public void setTargetBfPercent(Double targetBfPercent) {
        this.targetBfPercent = targetBfPercent;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}