package br.com.nutriplus.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-Id";
    public static final String TRACE_HEADER = "X-Trace-Id";
    public static final String FLOW_HEADER = "X-Flow-Id";
    public static final String SESSION_HEADER = "X-Session-Id";

    public static final String MDC_KEY = "correlationId";
    public static final String MDC_TRACE = "traceId";
    public static final String MDC_FLOW = "flowId";
    public static final String MDC_SESSION = "sessionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = resolveOrGenerate(request, HEADER);
        String traceId = resolveOrGenerate(request, TRACE_HEADER);

        response.setHeader(HEADER, correlationId);
        response.setHeader(TRACE_HEADER, traceId);

        MDC.put(MDC_KEY, correlationId);
        MDC.put(MDC_TRACE, traceId);
        putOptionalMdc(request, FLOW_HEADER, MDC_FLOW);
        putOptionalMdc(request, SESSION_HEADER, MDC_SESSION);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
            MDC.remove(MDC_TRACE);
            MDC.remove(MDC_FLOW);
            MDC.remove(MDC_SESSION);
        }
    }

    private static String resolveOrGenerate(HttpServletRequest request, String header) {
        String incoming = request.getHeader(header);
        if (incoming == null || incoming.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return incoming.strip();
    }

    private static void putOptionalMdc(HttpServletRequest request, String header, String mdcKey) {
        String value = request.getHeader(header);
        if (value != null && !value.isBlank()) {
            MDC.put(mdcKey, value.strip());
        }
    }
}
