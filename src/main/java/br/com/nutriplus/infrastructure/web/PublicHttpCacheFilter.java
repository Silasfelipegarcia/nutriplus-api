package br.com.nutriplus.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Cache-Control para GETs públicos e estáveis (CDN/browser).
 */
@Component
public class PublicHttpCacheFilter extends OncePerRequestFilter {

    private static final Map<String, String> CACHE_BY_PATH = Map.ofEntries(
            Map.entry("/training/sports", "public, max-age=86400"),
            Map.entry("/pricing/guidelines", "public, max-age=3600"),
            Map.entry("/legal/terms", "public, max-age=3600"),
            Map.entry("/legal/privacy", "public, max-age=3600"),
            Map.entry("/legal/ai-disclosure", "public, max-age=3600"),
            Map.entry("/legal/data-sharing-consent", "public, max-age=3600"),
            Map.entry("/legal/health-eligibility", "public, max-age=3600"),
            Map.entry("/legal/nutritionist-terms", "public, max-age=3600")
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (HttpMethod.GET.matches(request.getMethod())) {
            String cache = CACHE_BY_PATH.get(request.getRequestURI());
            if (cache != null) {
                response.setHeader("Cache-Control", cache);
            }
        }
        filterChain.doFilter(request, response);
    }
}
