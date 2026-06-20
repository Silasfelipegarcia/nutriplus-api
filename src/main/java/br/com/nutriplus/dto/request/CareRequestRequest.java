package br.com.nutriplus.dto.request;

import br.com.nutriplus.domain.enums.PreferredCareMode;

public record CareRequestRequest(
        PreferredCareMode preferredCareMode
) {
}
