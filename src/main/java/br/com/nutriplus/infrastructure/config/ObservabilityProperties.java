package br.com.nutriplus.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;
import java.util.Set;

@ConfigurationProperties(prefix = "nutriplus.observability")
public record ObservabilityProperties(
        @DefaultValue("1000") long slowRequestMs,
        @DefaultValue("300") long syncSlowRequestMs,
        List<String> syncFlowIds
) {

    private static final Set<String> DEFAULT_SYNC_FLOW_IDS = Set.of(
            "register",
            "login",
            "refresh-token",
            "users-me",
            "update-profile",
            "change-password",
            "accept-terms",
            "delete-account",
            "onboarding-metrics",
            "nutrition-profile",
            "checkins-today",
            "checkin-save",
            "checkin-extra",
            "checkins-stats",
            "meal-plan-latest",
            "shopping-list-latest",
            "progress-schedule",
            "progress-measurement",
            "progress-review",
            "progress-review-latest",
            "progress-evolution",
            "training-sports",
            "training-profile",
            "training-profile-save",
            "training-apply",
            "marketplace-list",
            "marketplace-detail",
            "accept-invite",
            "care-request",
            "care-contact",
            "consultation-pay",
            "care-my",
            "conversations-list",
            "conversation-send",
            "legal-terms",
            "legal-privacy",
            "legal-ai",
            "legal-consent",
            "analytics-events"
    );

    public boolean isSyncFlow(String flowId) {
        if (flowId == null || flowId.isBlank()) {
            return false;
        }
        if (syncFlowIds != null && !syncFlowIds.isEmpty()) {
            return syncFlowIds.contains(flowId);
        }
        return DEFAULT_SYNC_FLOW_IDS.contains(flowId);
    }
}
