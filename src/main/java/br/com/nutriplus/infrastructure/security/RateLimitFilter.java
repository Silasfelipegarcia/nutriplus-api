package br.com.nutriplus.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import br.com.nutriplus.infrastructure.web.CorrelationIdFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory IP rate limiter. For multi-replica deployments, enable Redis via {@code spring.data.redis.host}.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.enabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        boolean isAuthPath = path.startsWith("/auth");
        int requestLimit = isAuthPath ? properties.authRequestsPerWindow() : properties.generalRequestsPerWindow();

        String clientIp = ClientIpResolver.resolve(request, properties.trustedProxyIps());
        String key = clientIp + ":" + (isAuthPath ? "AUTH" : "GENERAL");
        long now = Instant.now().getEpochSecond();
        evictExpiredCounters(now);

        Counter counter = counters.compute(key, (ignored, current) -> {
            if (current == null || now - current.windowStartEpoch() >= properties.windowSeconds()) {
                return new Counter(now, 1);
            }
            return new Counter(current.windowStartEpoch(), current.count() + 1);
        });

        if (counter.count() > requestLimit) {
            writeTooManyRequests(response, "Muitas requisições. Tente novamente em instantes.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    static void writeTooManyRequests(HttpServletResponse response, String message) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("code", "RATE_LIMIT_EXCEEDED");
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (correlationId != null && !correlationId.isBlank()) {
            body.put("correlationId", correlationId);
        }
        String traceId = MDC.get(CorrelationIdFilter.MDC_TRACE);
        if (traceId != null && !traceId.isBlank()) {
            body.put("traceId", traceId);
        }
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }

    private void evictExpiredCounters(long now) {
        if (counters.size() < 1000) {
            return;
        }
        long window = properties.windowSeconds();
        Iterator<Map.Entry<String, Counter>> iterator = counters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Counter> entry = iterator.next();
            if (now - entry.getValue().windowStartEpoch() >= window) {
                iterator.remove();
            }
        }
    }

    private record Counter(long windowStartEpoch, int count) {
    }
}
