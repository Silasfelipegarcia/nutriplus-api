package br.com.nutriplus.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Plan {
    private final UUID id;
    private final UUID userId;
    private final Integer versionNumber;
    private final LocalDate startDate;
    private final List<PlanDay> days;

    public Plan(UUID id, UUID userId, Integer versionNumber, LocalDate startDate, List<PlanDay> days) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.versionNumber = Objects.requireNonNull(versionNumber);
        this.startDate = Objects.requireNonNull(startDate);
        this.days = days;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public List<PlanDay> getDays() {
        return days;
    }
}
