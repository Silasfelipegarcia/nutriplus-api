package br.com.nutriplus.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Allows Prometheus scraping via {@code X-Metrics-Token} without a user JWT.
 */
@Component
public class MetricsTokenFilter extends OncePerRequestFilter {

    public static final String METRICS_TOKEN_HEADER = "X-Metrics-Token";

    private final String metricsToken;

    public MetricsTokenFilter(@Value("${management.metrics.token:}") String metricsToken) {
        this.metricsToken = metricsToken == null ? "" : metricsToken;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/actuator/prometheus");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!shouldNotFilter(request) && !metricsToken.isBlank()) {
            String provided = request.getHeader(METRICS_TOKEN_HEADER);
            if (!metricsToken.equals(provided)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Invalid metrics token\"}");
                return;
            }
            request.setAttribute("metricsTokenAuthenticated", true);
        } else if (!metricsToken.isBlank()) {
            String provided = request.getHeader(METRICS_TOKEN_HEADER);
            if (metricsToken.equals(provided)) {
                request.setAttribute("metricsTokenAuthenticated", true);
            }
        }
        filterChain.doFilter(request, response);
    }

    public static boolean isMetricsTokenAuthenticated(HttpServletRequest request) {
        return Boolean.TRUE.equals(request.getAttribute("metricsTokenAuthenticated"));
    }
}
