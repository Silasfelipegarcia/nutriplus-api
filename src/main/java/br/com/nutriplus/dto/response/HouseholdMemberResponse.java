package br.com.nutriplus.dto.response;

import java.time.LocalDateTime;

public record HouseholdMemberResponse(
        Long userId,
        String name,
        String role,
        String status,
        LocalDateTime joinedAt
) {
}
