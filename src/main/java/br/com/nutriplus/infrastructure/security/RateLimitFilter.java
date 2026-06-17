package br.com.nutriplus.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limiter — suitable for single-instance deployments only.
 * For horizontal scaling, replace with Redis or gateway-level rate limiting.
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

        String key = request.getRemoteAddr() + ":" + (isAuthPath ? "AUTH" : "GENERAL");
        long now = Instant.now().getEpochSecond();
        evictExpiredCounters(now);

        Counter counter = counters.compute(key, (ignored, current) -> {
            if (current == null || now - current.windowStartEpoch() >= properties.windowSeconds()) {
                return new Counter(now, 1);
            }
            return new Counter(current.windowStartEpoch(), current.count() + 1);
        });

        if (counter.count() > requestLimit) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", "Muitas requisições. Tente novamente em instantes.")));
            return;
        }

        filterChain.doFilter(request, response);
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
