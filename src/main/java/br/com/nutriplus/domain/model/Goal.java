package br.com.nutriplus.domain.model;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Goal {
    private final UUID id;
    private final UUID userId;
    private final String type;
    private final String pace;
    private final Double targetWeightKg;
    private final Double targetBfPercent;
    private final OffsetDateTime createdAt;

    public Goal(UUID id, UUID userId, String type, String pace, Double targetWeightKg, Double targetBfPercent, OffsetDateTime createdAt) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.type = Objects.requireNonNull(type);
        this.pace = Objects.requireNonNull(pace);
        this.targetWeightKg = targetWeightKg;
        this.targetBfPercent = targetBfPercent;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public String getPace() {
        return pace;
    }

    public Double getTargetWeightKg() {
        return targetWeightKg;
    }

    public Double getTargetBfPercent() {
        return targetBfPercent;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
