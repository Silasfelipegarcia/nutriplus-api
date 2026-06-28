package br.com.nutriplus.dto.request;

import jakarta.validation.constraints.Size;

public record ProgressReviewRequest(
        @Size(max = 2000) String physicalDiscomforts,
        @Size(max = 2000) String positiveChanges,
        @Size(max = 2000) String generalNotes
) {
}
