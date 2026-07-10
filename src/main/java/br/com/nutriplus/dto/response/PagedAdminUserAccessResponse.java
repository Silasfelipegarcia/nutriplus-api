package br.com.nutriplus.dto.response;

import java.util.List;

public record PagedAdminUserAccessResponse(
        List<AdminUserAccessResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
