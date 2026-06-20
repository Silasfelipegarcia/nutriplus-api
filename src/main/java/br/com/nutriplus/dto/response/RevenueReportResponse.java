package br.com.nutriplus.dto.response;

import java.util.List;

public record RevenueReportResponse(
        int year,
        int month,
        int grossCents,
        int platformFeeCents,
        int netCents,
        int consultationCount,
        int averageTicketCents,
        List<MonthlyRevenuePoint> history
) {
    public record MonthlyRevenuePoint(int year, int month, int grossCents, int netCents) {
    }
}
