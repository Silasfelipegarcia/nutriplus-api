package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_app_feedback")
public class UserAppFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "survey_version", nullable = false, length = 20)
    private String surveyVersion = "v1";

    @Column(name = "trigger_context", nullable = false, length = 64)
    private String triggerContext = "MANUAL";

    @Column(name = "ease_of_use", nullable = false)
    private int easeOfUse;

    @Column(name = "meal_plan_quality", nullable = false)
    private int mealPlanQuality;

    @Column(name = "ai_helpfulness", nullable = false)
    private int aiHelpfulness;

    @Column(name = "progress_tracking", nullable = false)
    private int progressTracking;

    @Column(name = "overall_satisfaction", nullable = false)
    private int overallSatisfaction;

    @Column(name = "improvement_suggestions", columnDefinition = "TEXT")
    private String improvementSuggestions;

    @Column(name = "app_version", length = 32)
    private String appVersion;

    @Column(length = 16)
    private String platform;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected UserAppFeedback() {
    }

    public UserAppFeedback(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getSurveyVersion() {
        return surveyVersion;
    }

    public void setSurveyVersion(String surveyVersion) {
        this.surveyVersion = surveyVersion;
    }

    public String getTriggerContext() {
        return triggerContext;
    }

    public void setTriggerContext(String triggerContext) {
        this.triggerContext = triggerContext;
    }

    public int getEaseOfUse() {
        return easeOfUse;
    }

    public void setEaseOfUse(int easeOfUse) {
        this.easeOfUse = easeOfUse;
    }

    public int getMealPlanQuality() {
        return mealPlanQuality;
    }

    public void setMealPlanQuality(int mealPlanQuality) {
        this.mealPlanQuality = mealPlanQuality;
    }

    public int getAiHelpfulness() {
        return aiHelpfulness;
    }

    public void setAiHelpfulness(int aiHelpfulness) {
        this.aiHelpfulness = aiHelpfulness;
    }

    public int getProgressTracking() {
        return progressTracking;
    }

    public void setProgressTracking(int progressTracking) {
        this.progressTracking = progressTracking;
    }

    public int getOverallSatisfaction() {
        return overallSatisfaction;
    }

    public void setOverallSatisfaction(int overallSatisfaction) {
        this.overallSatisfaction = overallSatisfaction;
    }

    public String getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public void setImprovementSuggestions(String improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
