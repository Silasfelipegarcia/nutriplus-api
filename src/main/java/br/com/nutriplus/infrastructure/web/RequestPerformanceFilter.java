package br.com.nutriplus.infrastructure.web;

import br.com.nutriplus.infrastructure.config.ObservabilityProperties;
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

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestPerformanceFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestPerformanceFilter.class);

    private final ObservabilityProperties observabilityProperties;

    public RequestPerformanceFilter(ObservabilityProperties observabilityProperties) {
        this.observabilityProperties = observabilityProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.nanoTime();
        String method = request.getMethod();
        String path = request.getRequestURI();

        MDC.put("httpMethod", method);
        MDC.put("httpPath", path);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            int status = response.getStatus();

            MDC.put("httpStatus", String.valueOf(status));
            MDC.put("durationMs", String.valueOf(durationMs));

            if (durationMs > observabilityProperties.slowRequestMs()) {
                log.warn("[HTTP] {} {} {} {}ms (slow)", method, path, status, durationMs);
            } else if (log.isDebugEnabled()) {
                log.debug("[HTTP] {} {} {} {}ms", method, path, status, durationMs);
            }

            MDC.remove("httpMethod");
            MDC.remove("httpPath");
            MDC.remove("httpStatus");
            MDC.remove("durationMs");
        }
    }
}
