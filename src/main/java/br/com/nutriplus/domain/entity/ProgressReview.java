package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.ProgressReviewStatus;
import br.com.nutriplus.domain.enums.ProgressTrend;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "progress_reviews")
public class ProgressReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "current_session_id", nullable = false)
    private BodyMeasurementSession currentSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_session_id")
    private BodyMeasurementSession previousSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressReviewStatus status = ProgressReviewStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private ProgressTrend trend;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "week_adherence_percent")
    private Integer weekAdherencePercent;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "physical_discomforts", columnDefinition = "TEXT")
    private String physicalDiscomforts;

    @Column(name = "positive_changes", columnDefinition = "TEXT")
    private String positiveChanges;

    @Column(name = "general_notes", columnDefinition = "TEXT")
    private String generalNotes;

    @Column(name = "plan_change_suggested")
    private Boolean planChangeSuggested;

    @Column(name = "plan_change_rationale", columnDefinition = "TEXT")
    private String planChangeRationale;

    @Column(name = "keep_plan_message", columnDefinition = "TEXT")
    private String keepPlanMessage;

    @Column(name = "confidence", length = 20)
    private String confidence;

    @Column(name = "plan_regen_consumed", nullable = false)
    private boolean planRegenConsumed = false;

    protected ProgressReview() {
    }

    public ProgressReview(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BodyMeasurementSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(BodyMeasurementSession currentSession) {
        this.currentSession = currentSession;
    }

    public BodyMeasurementSession getPreviousSession() {
        return previousSession;
    }

    public void setPreviousSession(BodyMeasurementSession previousSession) {
        this.previousSession = previousSession;
    }

    public ProgressReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ProgressReviewStatus status) {
        this.status = status;
    }

    public ProgressTrend getTrend() {
        return trend;
    }

    public void setTrend(ProgressTrend trend) {
        this.trend = trend;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public Integer getWeekAdherencePercent() {
        return weekAdherencePercent;
    }

    public void setWeekAdherencePercent(Integer weekAdherencePercent) {
        this.weekAdherencePercent = weekAdherencePercent;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getPhysicalDiscomforts() {
        return physicalDiscomforts;
    }

    public void setPhysicalDiscomforts(String physicalDiscomforts) {
        this.physicalDiscomforts = physicalDiscomforts;
    }

    public String getPositiveChanges() {
        return positiveChanges;
    }

    public void setPositiveChanges(String positiveChanges) {
        this.positiveChanges = positiveChanges;
    }

    public String getGeneralNotes() {
        return generalNotes;
    }

    public void setGeneralNotes(String generalNotes) {
        this.generalNotes = generalNotes;
    }

    public Boolean getPlanChangeSuggested() {
        return planChangeSuggested;
    }

    public void setPlanChangeSuggested(Boolean planChangeSuggested) {
        this.planChangeSuggested = planChangeSuggested;
    }

    public String getPlanChangeRationale() {
        return planChangeRationale;
    }

    public void setPlanChangeRationale(String planChangeRationale) {
        this.planChangeRationale = planChangeRationale;
    }

    public String getKeepPlanMessage() {
        return keepPlanMessage;
    }

    public void setKeepPlanMessage(String keepPlanMessage) {
        this.keepPlanMessage = keepPlanMessage;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public boolean isPlanRegenConsumed() {
        return planRegenConsumed;
    }

    public void setPlanRegenConsumed(boolean planRegenConsumed) {
        this.planRegenConsumed = planRegenConsumed;
    }
}
