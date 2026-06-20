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
}
