package br.com.nutriplus.dto.response;

import java.util.List;

public record ProDashboardResponse(
        int activePatients,
        int preEngagedPatients,
        int pendingPaymentPatients,
        int monthlyConsultations,
        int monthlyGrossCents,
        int monthlyNetCents,
        List<CareRelationshipResponse> recentPatients
) {
}
