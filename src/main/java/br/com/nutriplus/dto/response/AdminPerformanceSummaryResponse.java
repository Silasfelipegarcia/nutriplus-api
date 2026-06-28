package br.com.nutriplus.dto.response;

import java.time.Instant;
import java.util.List;

public record AdminPerformanceSummaryResponse(
        Instant measuredAt,
        String environment,
        boolean cacheEnabled,
        boolean redisConfigured,
        int tierSThresholdMs,
        int dashboardFlowThresholdMs,
        double tierSAvgP95Ms,
        double tierSP95Ms,
        double dashboardFlowSumP95Ms,
        int criticalFailures,
        List<EndpointProbe> endpoints,
        String baselineDocPath,
        String auditScriptHint
) {
    public record EndpointProbe(
            String method,
            String path,
            String tier,
            String description,
            int status,
            long p95Ms,
            String grade,
            boolean slow,
            boolean failed
    ) {
    }
}
