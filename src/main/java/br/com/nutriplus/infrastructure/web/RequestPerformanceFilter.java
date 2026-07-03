package br.com.nutriplus.infrastructure.web;

import br.com.nutriplus.infrastructure.config.ObservabilityProperties;
import br.com.nutriplus.infrastructure.observability.NewRelicTraceBridge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestPerformanceFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestPerformanceFilter.class);
    private static final String CLIENT_DURATION_HEADER = "X-Client-Duration-Ms";

    private final ObservabilityProperties observabilityProperties;
    private final MeterRegistry meterRegistry;

    public RequestPerformanceFilter(ObservabilityProperties observabilityProperties,
                                    MeterRegistry meterRegistry) {
        this.observabilityProperties = observabilityProperties;
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.nanoTime();
        String method = request.getMethod();
        String path = request.getRequestURI();
        String flowId = request.getHeader(CorrelationIdFilter.FLOW_HEADER);

        MDC.put("httpMethod", method);
        MDC.put("httpPath", path);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            int status = response.getStatus();
            boolean sync = observabilityProperties.isSyncFlow(flowId);
            String flowTag = flowId != null && !flowId.isBlank() ? flowId : "unknown";

            MDC.put("httpStatus", String.valueOf(status));
            MDC.put("durationMs", String.valueOf(durationMs));

            recordMetrics(flowTag, sync, status, durationMs);

            String clientDuration = request.getHeader(CLIENT_DURATION_HEADER);
            if (clientDuration != null && !clientDuration.isBlank()) {
                MDC.put("clientDurationMs", clientDuration.strip());
            }

            NewRelicTraceBridge.syncFromMdc();
            NewRelicTraceBridge.syncHttpContext(method, path, status, durationMs);

            if (shouldLogHttpError(status, path)) {
                log.warn("[HTTP] {} {} {} {}ms (client-error flow={})", method, path, status, durationMs, flowTag);
            } else if (sync && durationMs > observabilityProperties.syncSlowRequestMs()) {
                log.warn("[HTTP] {} {} {} {}ms (sync-slow flow={})", method, path, status, durationMs, flowTag);
            } else if (durationMs > observabilityProperties.slowRequestMs()) {
                log.warn("[HTTP] {} {} {} {}ms (slow)", method, path, status, durationMs);
            } else if (log.isDebugEnabled()) {
                log.debug("[HTTP] {} {} {} {}ms", method, path, status, durationMs);
            }

            MDC.remove("httpMethod");
            MDC.remove("httpPath");
            MDC.remove("httpStatus");
            MDC.remove("durationMs");
            MDC.remove("clientDurationMs");
        }
    }

    private void recordMetrics(String flowTag, boolean sync, int status, long durationMs) {
        Timer.builder("nutriplus.http.server.duration")
                .tag("flow", flowTag)
                .tag("sync", String.valueOf(sync))
                .tag("status", String.valueOf(status))
                .publishPercentileHistogram()
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    private static boolean shouldLogHttpError(int status, String path) {
        if (status < 400) {
            return false;
        }
        return !path.startsWith("/actuator") && !"/health".equals(path);
    }
}
