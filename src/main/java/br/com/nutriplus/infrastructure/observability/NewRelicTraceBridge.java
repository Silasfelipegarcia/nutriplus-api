package br.com.nutriplus.infrastructure.observability;

import br.com.nutriplus.infrastructure.web.CorrelationIdFilter;
import br.com.nutriplus.infrastructure.web.IdempotencySupport;
import br.com.nutriplus.infrastructure.web.MdcUserFilter;
import com.newrelic.api.agent.NewRelic;
import org.slf4j.MDC;

/**
 * Propaga campos MDC para custom attributes da transaction New Relic (no-op sem javaagent).
 */
public final class NewRelicTraceBridge {

    private NewRelicTraceBridge() {
    }

    public static void syncFromMdc() {
        addIfPresent("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
        addIfPresent("traceId", MDC.get(CorrelationIdFilter.MDC_TRACE));
        addIfPresent("flowId", MDC.get(CorrelationIdFilter.MDC_FLOW));
        addIfPresent("sessionId", MDC.get(CorrelationIdFilter.MDC_SESSION));
        addIfPresent("userId", MDC.get(MdcUserFilter.MDC_USER));
        addIfPresent("idempotencyKey", MDC.get(IdempotencySupport.MDC_KEY));
    }

    public static void syncHttpContext(String method, String path, int status, long durationMs) {
        addIfPresent("httpMethod", method);
        addIfPresent("httpPath", path);
        NewRelic.addCustomParameter("httpStatus", status);
        NewRelic.addCustomParameter("durationMs", durationMs);
    }

    public static void noticeError(Throwable error) {
        if (error == null) {
            return;
        }
        syncFromMdc();
        NewRelic.noticeError(error);
    }

    private static void addIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            NewRelic.addCustomParameter(key, value);
        }
    }
}
