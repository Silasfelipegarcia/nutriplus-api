package br.com.nutriplus.dto.response;

public record AdminAccessSummaryResponse(
        long pendingApprovalCount,
        long loginEnabledCount,
        long adminCount,
        long pendingNutritionistCount,
        long totalUsers
) {
}
