package br.com.nutriplus.dto.response;

import java.time.LocalDate;
import java.util.List;

public record PlanRegenerationEligibilityResponse(
        List<String> allowedReasons,
        LocalDate lockedUntil,
        int daysUntilUnlock,
        boolean oneTimeCorrectionAvailable,
        boolean athleteRegenAvailable,
        boolean reviewDue,
        int daysUntilReview,
        LocalDate nextReviewDue,
        boolean hasMealPlan,
        Long pendingCycleReviewId,
        boolean aiPlanEligible,
        String aiPlanIneligibleReason,
        String aiPlanIneligibleMessagePt,
        boolean planResetAvailable,
        boolean currentPlanStarted,
        int currentPlanCheckinCount,
        int currentPlanDaysActive
) {
}
