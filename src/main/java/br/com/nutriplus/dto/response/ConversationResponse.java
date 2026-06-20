package br.com.nutriplus.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationResponse(
        Long threadId,
        Long careRelationshipId,
        String participantName,
        CareRelationshipResponse care,
        List<MessageResponse> messages
) {
}
