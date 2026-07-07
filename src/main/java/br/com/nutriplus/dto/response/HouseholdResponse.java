package br.com.nutriplus.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record HouseholdResponse(
        Long id,
        Long ownerUserId,
        String ownerName,
        Long baseMealPlanId,
        int memberCount,
        int maxMembers,
        List<HouseholdMemberResponse> members,
        List<HouseholdInvitationResponse> pendingInvitations,
        LocalDateTime createdAt
) {
}
