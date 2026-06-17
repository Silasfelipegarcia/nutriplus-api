package br.com.nutriplus.infrastructure.web;

import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public final class TraceContext {

    private TraceContext() {
    }

    public static Map<String, String> currentHeaders() {
        Map<String, String> headers = new HashMap<>();
        put(headers, CorrelationIdFilter.HEADER, MDC.get(CorrelationIdFilter.MDC_KEY));
        put(headers, CorrelationIdFilter.TRACE_HEADER, MDC.get(CorrelationIdFilter.MDC_TRACE));
        put(headers, CorrelationIdFilter.FLOW_HEADER, MDC.get(CorrelationIdFilter.MDC_FLOW));
        put(headers, CorrelationIdFilter.SESSION_HEADER, MDC.get(CorrelationIdFilter.MDC_SESSION));
        return headers;
    }

    private static void put(Map<String, String> headers, String name, String value) {
        if (value != null && !value.isBlank()) {
            headers.put(name, value);
        }
    }
}
