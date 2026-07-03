package br.com.nutriplus.infrastructure.observability;

import br.com.nutriplus.infrastructure.web.CorrelationIdFilter;
import br.com.nutriplus.infrastructure.web.IdempotencySupport;
import br.com.nutriplus.infrastructure.web.MdcUserFilter;
import org.slf4j.MDC;

/**
 * Propaga campos MDC para custom attributes da transaction New Relic.
 * Sem agente NR, {@code newrelic-api} expõe stubs no-op (nunca lança).
 */
public final class NewRelicTraceBridge {

    private static volatile boolean disabled;

    private NewRelicTraceBridge() {
    }

    public static void syncFromMdc() {
        if (disabled) {
            return;
        }
        try {
            addIfPresent("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
            addIfPresent("traceId", MDC.get(CorrelationIdFilter.MDC_TRACE));
            addIfPresent("flowId", MDC.get(CorrelationIdFilter.MDC_FLOW));
            addIfPresent("sessionId", MDC.get(CorrelationIdFilter.MDC_SESSION));
            addIfPresent("userId", MDC.get(MdcUserFilter.MDC_USER));
            addIfPresent("idempotencyKey", MDC.get(IdempotencySupport.MDC_KEY));
        } catch (LinkageError | Exception ex) {
            disableAfterFailure(ex);
        }
    }

    public static void syncHttpContext(String method, String path, int status, long durationMs) {
        if (disabled) {
            return;
        }
        try {
            addIfPresent("httpMethod", method);
            addIfPresent("httpPath", path);
            com.newrelic.api.agent.NewRelic.addCustomParameter("httpStatus", status);
            com.newrelic.api.agent.NewRelic.addCustomParameter("durationMs", durationMs);
        } catch (LinkageError | Exception ex) {
            disableAfterFailure(ex);
        }
    }

    public static void noticeError(Throwable error) {
        if (disabled || error == null) {
            return;
        }
        try {
            syncFromMdc();
            com.newrelic.api.agent.NewRelic.noticeError(error);
        } catch (LinkageError | Exception ex) {
            disableAfterFailure(ex);
        }
    }

    private static void addIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            com.newrelic.api.agent.NewRelic.addCustomParameter(key, value);
        }
    }

    private static void disableAfterFailure(Throwable ex) {
        disabled = true;
        org.slf4j.LoggerFactory.getLogger(NewRelicTraceBridge.class)
                .debug("New Relic bridge disabled: {}", ex.toString());
    }

    /** Test hook — reset disabled flag between tests. */
    static void resetForTests() {
        disabled = false;
    }
}
