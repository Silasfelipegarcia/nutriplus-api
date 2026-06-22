package br.com.nutriplus.infrastructure.security;

import br.com.nutriplus.infrastructure.web.CachedBodyHttpServletRequest;
import br.com.nutriplus.infrastructure.web.CorrelationIdFilter;
import br.com.nutriplus.service.SecurityRiskService;
import br.com.nutriplus.service.SecurityRiskService.RiskAction;
import br.com.nutriplus.service.SecurityRiskService.RiskContext;
import br.com.nutriplus.service.SecurityRiskService.RiskDecision;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class RiskEvaluationFilter extends OncePerRequestFilter {

    private static final Set<String> EXPENSIVE_PATHS = Set.of(
            "/meal-plans/generate",
            "/nutrition-profile"
    );

    private final SecurityRiskService securityRiskService;
    private final RateLimitProperties rateLimitProperties;
    private final ObjectMapper objectMapper;

    public RiskEvaluationFilter(SecurityRiskService securityRiskService,
                                RateLimitProperties rateLimitProperties,
                                ObjectMapper objectMapper) {
        this.securityRiskService = securityRiskService;
        this.rateLimitProperties = rateLimitProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/health")
                || path.startsWith("/actuator")
                || path.startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        HttpServletRequest effective = request;
        String body = null;

        if (HttpMethod.POST.matches(request.getMethod()) && EXPENSIVE_PATHS.contains(request.getRequestURI())) {
            CachedBodyHttpServletRequest cached = request instanceof CachedBodyHttpServletRequest existing
                    ? existing
                    : new CachedBodyHttpServletRequest(request);
            body = cached.bodyAsString();
            effective = cached;

            RiskDecision decision = securityRiskService.evaluate(new RiskContext(
                    request.getRequestURI(),
                    ClientIpResolver.resolve(request, rateLimitProperties.trustedProxyIps()),
                    currentUserId(),
                    MDC.get(CorrelationIdFilter.MDC_KEY),
                    body,
                    false,
                    false,
                    true
            ));

            if (decision.action() == RiskAction.BLOCK) {
                writeBlocked(response, decision);
                return;
            }
            if (decision.action() == RiskAction.THROTTLE) {
                RateLimitFilter.writeTooManyRequests(response, "Requisição temporariamente limitada por segurança.");
                return;
            }
        }

        filterChain.doFilter(effective, response);

        if (response.getStatus() == 401 || response.getStatus() == 429) {
            securityRiskService.evaluate(new RiskContext(
                    request.getRequestURI(),
                    ClientIpResolver.resolve(request, rateLimitProperties.trustedProxyIps()),
                    currentUserId(),
                    MDC.get(CorrelationIdFilter.MDC_KEY),
                    body,
                    response.getStatus() == 401,
                    response.getStatus() == 429,
                    EXPENSIVE_PATHS.contains(request.getRequestURI())
            ));
        }
    }

    private void writeBlocked(HttpServletResponse response, RiskDecision decision) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", "Requisição bloqueada por política de segurança.");
        payload.put("code", "SECURITY_RISK_BLOCKED");
        payload.put("score", decision.score());
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (correlationId != null) {
            payload.put("correlationId", correlationId);
        }
        response.getWriter().write(objectMapper.writeValueAsString(payload));
    }

    private static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            try {
                return Long.parseLong(jwt.getSubject());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
