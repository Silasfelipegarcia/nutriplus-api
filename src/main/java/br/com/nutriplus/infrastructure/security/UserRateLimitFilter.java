package br.com.nutriplus.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-user limits on expensive Tier B/C endpoints (runs after JWT authentication).
 */
@Component
public class UserRateLimitFilter extends OncePerRequestFilter {

    private static final long HOUR_SECONDS = 3600L;

    private final RateLimitProperties properties;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    public UserRateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.enabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = currentUserId();
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();
        Integer limit = resolveLimit(path, method);
        if (limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = userId + ":" + path + ":" + method;
        long now = Instant.now().getEpochSecond();
        evictExpired(now);

        Counter counter = counters.compute(key, (ignored, current) -> {
            if (current == null || now - current.windowStartEpoch() >= HOUR_SECONDS) {
                return new Counter(now, 1);
            }
            return new Counter(current.windowStartEpoch(), current.count() + 1);
        });

        if (counter.count() > limit) {
            RateLimitFilter.writeTooManyRequests(
                    response,
                    "Limite de uso atingido para esta ação. Tente novamente mais tarde."
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Integer resolveLimit(String path, String method) {
        if (HttpMethod.POST.matches(method) && "/meal-plans/generate".equals(path)) {
            return properties.mealPlanGeneratePerHour();
        }
        if (HttpMethod.POST.matches(method) && "/nutrition-profile".equals(path)) {
            return properties.nutritionProfilePerHour();
        }
        return null;
    }

    private static String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return null;
    }

    private void evictExpired(long now) {
        if (counters.size() < 500) {
            return;
        }
        Iterator<Map.Entry<String, Counter>> iterator = counters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Counter> entry = iterator.next();
            if (now - entry.getValue().windowStartEpoch() >= HOUR_SECONDS) {
                iterator.remove();
            }
        }
    }

    private record Counter(long windowStartEpoch, int count) {
    }
}
